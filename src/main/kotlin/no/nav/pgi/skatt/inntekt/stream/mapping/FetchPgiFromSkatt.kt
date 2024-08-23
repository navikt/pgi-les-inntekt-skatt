package no.nav.pgi.skatt.inntekt.stream.mapping

import no.nav.pensjon.samhandling.maskfnr.maskFnr
import no.nav.pgi.domain.Hendelse
import no.nav.pgi.domain.PensjonsgivendeInntektMetadata
import no.nav.pgi.skatt.inntekt.skatt.PgiClient
import org.apache.kafka.streams.kstream.ValueMapper
import java.net.http.HttpResponse

internal class FetchPgiFromSkatt(private val pgiClient: PgiClient) :
    ValueMapper<Hendelse, PgiResponse> {

    override fun apply(hendelse: Hendelse): PgiResponse {
        try {
            val request = pgiClient.createPgiRequest(hendelse.gjelderPeriode!!, hendelse.identifikator!!) // TODO: String?
            return PgiResponse(
                pgiClient.getPgi(request, HttpResponse.BodyHandlers.ofString()),
                createPgiMetadata(hendelse),
                hendelse.identifikator!! // TODO: String?
            )
        } catch (e: Exception) {
            throw PensjonsgivendeInntektClientException("Call to pgi failed with exception: ${e.javaClass.simpleName}. Message: ${e.message} ")
        }
    }

    private fun createPgiMetadata(hendelse: Hendelse): PensjonsgivendeInntektMetadata =
        PensjonsgivendeInntektMetadata(hendelse.metaData!!.retries, hendelse.sekvensnummer) // TODO: metadata?
}

class PensjonsgivendeInntektClientException(message: String) : RuntimeException(message.maskFnr())