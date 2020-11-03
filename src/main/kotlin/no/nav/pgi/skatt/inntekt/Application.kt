package no.nav.pgi.skatt.inntekt

import no.nav.pensjon.samhandling.naisserver.naisServer
import no.nav.pgi.skatt.inntekt.stream.KafkaConfig
import no.nav.pgi.skatt.inntekt.stream.PensjonsgivendeInntektStream


fun main() {
    val application = Application()
    application.startPensjonsgivendeInntektStream()
}

internal class Application(kafkaConfig: KafkaConfig = KafkaConfig(), pensjonsgivendeInntektClient: PensjonsgivendeInntektClient = PensjonsgivendeInntektClient()) {
    private val pensjonsgivendeInntektStream = PensjonsgivendeInntektStream(kafkaConfig.streamConfig(), pensjonsgivendeInntektClient)

    init {
        val naisServer = naisServer()
        naisServer.start()
    }

    internal fun startPensjonsgivendeInntektStream() {
        pensjonsgivendeInntektStream.start()
    }

    internal fun stopPensjonsgivendeInntektStream() {
        pensjonsgivendeInntektStream.close()
    }
}


