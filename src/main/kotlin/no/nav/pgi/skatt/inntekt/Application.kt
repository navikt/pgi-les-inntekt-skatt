package no.nav.pgi.skatt.inntekt

import no.nav.pensjon.samhandling.naisserver.naisServer
import no.nav.pgi.skatt.inntekt.stream.KafkaConfig
import no.nav.pgi.skatt.inntekt.stream.PGIStream


fun main() {
    val application = Application()
    application.startPensjonsgivendeInntektStream()
}

internal class Application(kafkaConfig: KafkaConfig = KafkaConfig(), pgiClient: PgiClient = PgiClient()) {
    private val pensjonsgivendeInntektStream = PGIStream(kafkaConfig.streamConfig(), pgiClient)

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


