package no.nav.pgi.skatt.inntekt

import no.nav.pensjon.samhandling.naisserver.naisServer
import no.nav.pgi.skatt.inntekt.stream.KafkaConfig
import no.nav.pgi.skatt.inntekt.stream.PensjonsgivendeInntektStream


fun main() {
    val application = Application()
    application.startPensjonsgivendeInntektStream()
}

internal class Application(kafkaConfig: KafkaConfig = KafkaConfig(), skattClient: SkattClient = SkattClient()) {
    private val pensjonsgivendeInntektStream = PensjonsgivendeInntektStream(kafkaConfig.streamConfig(), skattClient)

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


