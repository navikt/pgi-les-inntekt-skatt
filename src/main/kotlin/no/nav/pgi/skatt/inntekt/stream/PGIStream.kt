package no.nav.pgi.skatt.inntekt.stream

import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.errors.StreamsException
import org.slf4j.LoggerFactory
import java.util.*


internal class PGIStream(streamProperties: Properties, pgiTopology: PGITopology) {

    private val pgiStream = KafkaStreams(pgiTopology.topology(), streamProperties)

    init{
        setStreamStateListener()
    }

    internal fun setUncaughtStreamExceptionHandler(handleException: (e: Throwable?) -> Unit) {
        // TODO: erstatt med StreamsUncaughtExceptionHandler
        pgiStream.setUncaughtExceptionHandler { thread: Thread?, e: Throwable? ->
            if (e is StreamsException && e.cause != null) {
                LOG.error("Uncaught StreamsException in thread $thread", e.cause)
            } else {
                LOG.error("Uncaught exception in thread $thread", e)
            }
            handleException(e)
        }
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

    internal fun isRunning() = pgiStream.state().isRunningOrRebalancing

    private companion object {
        private val LOG = LoggerFactory.getLogger(PGIStream::class.java)
    }
}