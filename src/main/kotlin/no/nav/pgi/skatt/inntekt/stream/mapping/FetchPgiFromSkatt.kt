package no.nav.pgi.skatt.inntekt.stream.mapping

import no.nav.pensjon.samhandling.maskfnr.maskFnr
import no.nav.pgi.skatt.inntekt.skatt.PgiClient
import no.nav.samordning.pgi.schema.Hendelse
import org.apache.kafka.streams.kstream.ValueMapper
import java.net.http.HttpResponse

internal class FetchPgiFromSkatt(private val pgiClient: PgiClient) : ValueMapper<Hendelse, HttpResponse<String>> {

    override fun apply(hendelse: Hendelse): HttpResponse<String> {
        try {
            val request = pgiClient.createPensjonsgivendeInntekterRequest(hendelse.getGjelderPeriode(), hendelse.getIdentifikator())
            return pgiClient.getPensjonsgivendeInntekter(request, HttpResponse.BodyHandlers.ofString())
        } catch (e: Exception) {
            throw PensjonsgivendeInntektClientException("Call to pgi failed with exception: ${e.javaClass.simpleName}. Message: \n ${e.message}")
        }
    }
}

class PensjonsgivendeInntektClientException(message: String) : RuntimeException(message.maskFnr())