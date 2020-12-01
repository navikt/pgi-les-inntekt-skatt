package no.nav.pgi.skatt.inntekt

import io.ktor.server.netty.*
import no.nav.pensjon.samhandling.naisserver.naisServer
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
        naisServer.start()
        addShutdownHook(naisServer)
    }

    internal fun startPensjonsgivendeInntektStream() {
        pensjonsgivendeInntektStream.start()
        addShutdownHook(pensjonsgivendeInntektStream)
    }

    internal fun stop() {
        pensjonsgivendeInntektStream.close()
        naisServer.stop(500, 500)
    }

    private fun addShutdownHook(naisServer: NettyApplicationEngine) {
        Runtime.getRuntime().addShutdownHook(Thread {
            try {
                LOGGER.info("stopping naisServer in shutdownHook")
                naisServer.stop(400, 400)
            } catch (e: Exception) {
                LOGGER.error("Error while shutting down naisServer", e)
            }
        })
    }

    private fun addShutdownHook(stream: PGIStream) {
        Runtime.getRuntime().addShutdownHook(Thread {
            try {
                LOGGER.info("stopping pgiStream in shutdownHook")
                stream.close()
            } catch (e: Exception) {
                LOGGER.error("Error while shutting down PgiStream", e)
            }
        })
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(Application::class.java)
    }
}


