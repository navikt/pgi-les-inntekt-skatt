package no.nav.pgi.skatt.inntekt.stream

import java.util.Properties
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler
import org.slf4j.LoggerFactory


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

        try {
            KafkaConsumer<String, ByteArray>(consumerProps).use { consumer ->
                val partitions = inputTopics.flatMap { topic ->
                    consumer.partitionsFor(topic).map { TopicPartition(it.topic(), it.partition()) }
                }

                if (partitions.isEmpty()) {
                    LOG.info("[${context}] No partitions found for topics: $inputTopics")
                    return
                }

                val committed = consumer.committed(partitions.toSet())
                val endOffsets = consumer.endOffsets(partitions)

                committed.forEach { (tp, offsetAndMetadata) ->
                    val offset = offsetAndMetadata?.offset()
                    val logTimestamp = System.currentTimeMillis()
                    val endOffset = endOffsets[tp]
                    val lag = if (offset != null && endOffset != null) (endOffset - offset).coerceAtLeast(0) else null

                    LOG.info("[${context}] Committed offset for ${tp.topic()}-${tp.partition()}: offset=$offset, timestamp=$logTimestamp, logEndOffset=$endOffset${if (lag != null) ", lag=$lag" else ""}")
                }
            }
        } catch (e: Exception) {
            LOG.warn("[${context}] Could not log committed offsets", e)
        }
    }

    internal fun start() {
        LOG.info("Starting PgiStream")
        logCommittedOffsets("startup")
        pgiStream.start()
        scheduler = Executors.newSingleThreadScheduledExecutor { runnable ->
            Thread(runnable, "PGIStream-Scheduler").apply {
                isDaemon = true
            }
        }
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
            Thread.currentThread().interrupt()
        }
        pgiStream.close()
        logCommittedOffsets("shutdown")
    }

    private companion object {
        private val LOG = LoggerFactory.getLogger(PGIStream::class.java)
    }
}
