package no.nav.pgi.skatt.inntekt.stream.mapping

import no.nav.pensjon.samhandling.maskfnr.maskFnr
import no.nav.pgi.skatt.inntekt.skatt.PgiClient
import no.nav.samordning.pgi.schema.Hendelse
import no.nav.samordning.pgi.schema.PensjonsgivendeInntektMetadata
import org.apache.kafka.streams.kstream.ValueMapper
import java.net.http.HttpResponse

internal class FetchPgiFromSkatt(private val pgiClient: PgiClient) :
    ValueMapper<Hendelse, PgiResponse> {

    override fun apply(hendelse: Hendelse): PgiResponse {
        try {
            val request = pgiClient.createPgiRequest(hendelse.getGjelderPeriode(), hendelse.getIdentifikator())
            return PgiResponse(
                pgiClient.getPgi(request, HttpResponse.BodyHandlers.ofString()),
                createPgiMetadata(hendelse),
                hendelse.getIdentifikator()
            )
        } catch (e: Exception) {
            throw PensjonsgivendeInntektClientException("Call to pgi failed with exception: ${e.javaClass.simpleName}. Message: ${e.message} ")
        }
    }

    private fun createPgiMetadata(hendelse: Hendelse): PensjonsgivendeInntektMetadata =
        PensjonsgivendeInntektMetadata(hendelse.getMetaData().getRetries(), hendelse.getSekvensnummer())
}

class PensjonsgivendeInntektClientException(message: String) : RuntimeException(message.maskFnr())