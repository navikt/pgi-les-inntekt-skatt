package no.nav.pgi.skatt.inntekt.stream

import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler
import org.slf4j.LoggerFactory
import java.util.*


internal class PGIStream(val streamProperties: Properties,  val pgiTopology: PGITopology) {

    private val pgiStream = createKafkaStream()

    init {
        setStreamStateListener()
    }

    fun createKafkaStream(): KafkaStreams {
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

    internal fun start() {
        LOG.info("Starting PgiStream")
        pgiStream.start()
    }

    internal fun close() {
        LOG.info("Closing PgiStream")
        pgiStream.close()
    }

    private companion object {
        private val LOG = LoggerFactory.getLogger(PGIStream::class.java)
    }
}

