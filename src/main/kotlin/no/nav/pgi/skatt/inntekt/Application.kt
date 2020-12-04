package no.nav.pgi.skatt.inntekt

import io.ktor.server.netty.*
import no.nav.pensjon.samhandling.naisserver.naisServer
import no.nav.pgi.skatt.inntekt.skatt.PgiClient
import no.nav.pgi.skatt.inntekt.stream.KafkaConfig
import no.nav.pgi.skatt.inntekt.stream.PGIStream
import no.nav.pgi.skatt.inntekt.stream.PGITopology
import org.slf4j.LoggerFactory


fun main() {
    try {
        val application = Application()
        application.startPensjonsgivendeInntektStream()
    } catch (e: java.lang.Exception) {
        System.exit(1)
    }
}

internal class Application(kafkaConfig: KafkaConfig = KafkaConfig(), pgiClient: PgiClient = PgiClient()) {
    private val pgiStream = PGIStream(kafkaConfig.streamProperties(), PGITopology(pgiClient))
    private val naisServer: NettyApplicationEngine = naisServer(readyCheck = { pgiStream.isRunning() })

    init {
        addShutdownHook()
        naisServer.start()
    }

    internal fun startPensjonsgivendeInntektStream() {
        pgiStream.start()
    }

    internal fun close() {
        pgiStream.close()
        naisServer.stop(500, 500)
    }

    private fun addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(Thread {
            try {
                close()
            } catch (e: Exception) {
                LOGGER.error("Error while shutting down", e)
            }
        })
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(Application::class.java)
    }
}

//TODO Sjekk om stream kaster exception. Eventuelt få den til å gjøre det.
//TODO Lag flere tester for DTO mapping og avromapping
//TODO dobbelsjekk DTO mapping og avromapping logikk.


