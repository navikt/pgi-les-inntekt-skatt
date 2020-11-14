package no.nav.pgi.skatt.inntekt.stream.mapping

import no.nav.pgi.skatt.inntekt.PgiClient
import no.nav.samordning.pgi.schema.Hendelse
import org.apache.kafka.streams.kstream.ValueMapper
import java.net.http.HttpResponse

internal class FetchPgiFromSkatt(private val pgiClient: PgiClient) : ValueMapper<Hendelse, HttpResponse<String>> {

    override fun apply(hendelse: Hendelse): HttpResponse<String> {
        val request = pgiClient.createPensjonsgivendeInntekterRequest(hendelse.getGjelderPeriode(), hendelse.getIdentifikator())
        return pgiClient.getPensjonsgivendeInntekter(request, HttpResponse.BodyHandlers.ofString())
    }
}