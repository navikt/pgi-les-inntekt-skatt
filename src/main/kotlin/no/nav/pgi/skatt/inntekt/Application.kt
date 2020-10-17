package no.nav.pgi.skatt.inntekt

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import no.nav.pensjon.samhandling.liveness.isAlive
import no.nav.pensjon.samhandling.liveness.isReady
import no.nav.pensjon.samhandling.metrics.metrics
import no.nav.pgi.skatt.inntekt.kafka.KafkaConfig


fun main() {
    val application = Application()
    application.startPensjonsgivendeInntektStream()
}

internal class Application(kafkaConfig: KafkaConfig = KafkaConfig()) {
    private val pensjonsgivendeInntektStream = PensjonsgivendeInntektStream(kafkaConfig.streamConfig())

    init {
        val naisServer = embeddedServer(Netty, createApplicationEnvironment())
        naisServer.start()
    }

    private fun createApplicationEnvironment(serverPort: Int = 8080) =
            applicationEngineEnvironment {
                connector { port = serverPort }
                module {
                    isAlive()
                    isReady()
                    metrics()
                }
            }

    internal fun startPensjonsgivendeInntektStream() {
        //pensjonsgivendeInntektStream.start()
    }

    internal fun stopPensjonsgivendeInntektStream() {
        pensjonsgivendeInntektStream.close()
    }
}


