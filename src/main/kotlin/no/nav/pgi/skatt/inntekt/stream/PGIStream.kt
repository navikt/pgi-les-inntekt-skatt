package no.nav.pgi.skatt.inntekt.stream

import org.apache.kafka.streams.KafkaStreams
import org.slf4j.LoggerFactory
import java.util.*


internal class PGIStream(streamProperties: Properties, pgiTopology: PGITopology) {

    private val pensjonsgivendeInntektStream = KafkaStreams(pgiTopology.topology(), streamProperties)

    init {
        setStreamStateListener()
        setUncaughtStreamExceptionHandler()
    }

    private fun setUncaughtStreamExceptionHandler() {
        pensjonsgivendeInntektStream.setUncaughtExceptionHandler { thread: Thread?, e: Throwable? ->
            LOG.error("Uncaught exception in thread $thread, closing beregnetSkattStream", e)
            pensjonsgivendeInntektStream.close()
        }
    }

    private fun setStreamStateListener() {
        pensjonsgivendeInntektStream.setStateListener { newState: KafkaStreams.State?, oldState: KafkaStreams.State? ->
            LOG.info("State change from $oldState to $newState")
        }
    }

    internal fun start() {
        LOG.info("Starting PgiStream")
        pensjonsgivendeInntektStream.start()
    }

    internal fun close() {
        LOG.info("Stopping PgiStream")
        pensjonsgivendeInntektStream.close()
    }

    internal fun isRunning() = pensjonsgivendeInntektStream.state().isRunningOrRebalancing

    private companion object {
        private val LOG = LoggerFactory.getLogger(PGIStream::class.java)
    }
}

