package no.nav.pgi.skatt.inntekt.stream

import java.util.Properties
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler
import org.slf4j.LoggerFactory
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.ByteArrayDeserializer


internal class PGIStream(val streamProperties: Properties, val pgiTopology: PGITopology) {

    private val pgiStream = createKafkaStream()
    private var scheduler: ScheduledExecutorService? = null

    init {
        setStreamStateListener()
    }

    fun createKafkaStream(): KafkaStreams {
        streamProperties.setProperty("commit.interval.ms", "1000") // commit interval to 1 second (1000 ms)
        val streams = KafkaStreams(pgiTopology.topology(), streamProperties)
        streams.setUncaughtExceptionHandler { e: Throwable? ->
            LOG.error("Uncaught exception in kafka stream", e)
            StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.REPLACE_THREAD
        }
        return streams
    }

    private fun setStreamStateListener() {
        pgiStream.setStateListener { newState: KafkaStreams.State?, oldState: KafkaStreams.State? ->
            LOG.info("State change from $oldState to $newState")
        }
    }

    private fun logCommittedOffsets(context: String) {
        val groupId = streamProperties.getProperty("application.id")
        val bootstrapServers = streamProperties.getProperty("bootstrap.servers")
        val inputTopics = pgiTopology.inputTopics()
        val consumerProps = Properties().apply {
            put("bootstrap.servers", bootstrapServers)
            put("group.id", groupId)
            put("enable.auto.commit", "false")
            put("key.deserializer", StringDeserializer::class.java.name)
            put("value.deserializer", ByteArrayDeserializer::class.java.name)
        }
        KafkaConsumer<String, ByteArray>(consumerProps).use { consumer ->
            val partitions = inputTopics.flatMap { topic ->
                consumer.partitionsFor(topic).map { TopicPartition(topic, it.partition()) }
            }
            val committed = consumer.committed(partitions.toSet())
            val endOffsets = consumer.endOffsets(partitions)
            committed.forEach { (tp, offsetAndMetadata) ->
                val offset = offsetAndMetadata?.offset()
                val logTimestamp = System.currentTimeMillis()
                val endOffset = endOffsets[tp]
                LOG.info("[${context}] Committed offset for ${tp.topic()}-${tp.partition()}: offset=$offset, partition=${tp.partition()}, timestamp=$logTimestamp, logEndOffset=$endOffset")
            }
        }
    }

    internal fun start() {
        LOG.info("Starting PgiStream")
        logCommittedOffsets("startup")
        pgiStream.start()
        scheduler = Executors.newSingleThreadScheduledExecutor()
        scheduler?.scheduleAtFixedRate({ logCommittedOffsets("running") }, 30, 30, TimeUnit.SECONDS)
    }

    internal fun close() {
        LOG.info("Closing PgiStream")
        scheduler?.shutdown()
        try {
            if (scheduler?.awaitTermination(5, TimeUnit.SECONDS) == false) {
                scheduler?.shutdownNow()
            }
        } catch (e: InterruptedException) {
            scheduler?.shutdownNow()
        }
        logCommittedOffsets("shutdown")
        pgiStream.close()
    }

    private companion object {
        private val LOG = LoggerFactory.getLogger(PGIStream::class.java)
    }
}
