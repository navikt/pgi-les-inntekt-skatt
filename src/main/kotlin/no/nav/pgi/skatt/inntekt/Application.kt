package no.nav.pgi.skatt.inntekt

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import no.nav.pensjon.samhandling.naisserver.naisServer
import no.nav.pgi.skatt.inntekt.skatt.PgiClient
import no.nav.pgi.skatt.inntekt.stream.KafkaConfig
import no.nav.pgi.skatt.inntekt.stream.PGIStream
import no.nav.pgi.skatt.inntekt.stream.PGITopology
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess


fun main() {
    try {
        val application = Application()
        application.start()
    } catch (e: Throwable) {
        exitProcess(1)
    }
}

internal class Application(kafkaConfig: KafkaConfig = KafkaConfig(), pgiClient: PgiClient = PgiClient()) {
    private val pgiStream = PGIStream(kafkaConfig.streamProperties(), PGITopology(pgiClient))
    private val naisServer: NettyApplicationEngine = naisServer(readyCheck = { pgiStream.isRunning() })

    internal fun start() {
        addShutdownHook()
        addCloseOnExceptionInStream()

        naisServer.start(wait = false)
        pgiStream.start()
    }

    internal fun stop() {
        LOG.info("Stop is called closing pgiStream and naisServer")
        Thread { pgiStream.close() }.start()
        naisServer.stop(5, 10, TimeUnit.SECONDS)
    }

    private fun addShutdownHook() {
        LOG.info("Adding shutdown hook")
        Runtime.getRuntime().addShutdownHook(Thread {
            try {
                LOG.info("Shutdown hook triggered closing pgiStream and naisServer")
                pgiStream.close()
                naisServer.stop(5, 10, TimeUnit.SECONDS)
            } catch (e: Exception) {
                LOG.error("Error while shutting down", e)
            }
        })
    }

    private fun addCloseOnExceptionInStream() = pgiStream.setUncaughtStreamExceptionHandler { stop() }

    companion object {
        private val LOG = LoggerFactory.getLogger(Application::class.java)
    }
}


