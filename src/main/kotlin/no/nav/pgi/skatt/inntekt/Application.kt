package no.nav.pgi.skatt.inntekt

import no.nav.pensjon.samhandling.naisserver.naisServer
import no.nav.pgi.skatt.inntekt.skatt.PgiClient
import no.nav.pgi.skatt.inntekt.stream.KafkaConfig
import no.nav.pgi.skatt.inntekt.stream.PGIStream
import no.nav.pgi.skatt.inntekt.stream.PGITopology
import org.slf4j.LoggerFactory


fun main() {
    val application = Application()
    application.startPensjonsgivendeInntektStream()
}

internal class Application(kafkaConfig: KafkaConfig = KafkaConfig(), pgiClient: PgiClient = PgiClient()) {
    private val pensjonsgivendeInntektStream = PGIStream(kafkaConfig.streamProperties(), PGITopology(pgiClient))
    private val naisServer = naisServer(readyCheck = { pensjonsgivendeInntektStream.isRunning() })

    init {
        addShutdownHook()
        naisServer.start()
    }

    internal fun startPensjonsgivendeInntektStream() {
        pensjonsgivendeInntektStream.start()
    }

    private fun addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(Thread {
            try {
                stop()
            } catch (e: Exception) {
                LOGGER.error("Error while shutting down", e)
            }
        })
    }

    internal fun stop() {
        pensjonsgivendeInntektStream.close()
        naisServer.stop(500, 500)
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(Application::class.java)
    }
}


