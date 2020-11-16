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
    private val naisServer = naisServer()

    init {
        naisServer.start()
    }

    internal fun startPensjonsgivendeInntektStream() {
        pensjonsgivendeInntektStream.start()
    }

    internal fun stop() {
        pensjonsgivendeInntektStream.close()
        naisServer.stop(500, 500)
    }
}


