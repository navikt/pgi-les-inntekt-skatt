package no.nav.pgi.skatt.inntekt.stream

import no.nav.pgi.skatt.inntekt.SkattClient
import no.nav.samordning.pgi.schema.Hendelse
import org.apache.kafka.streams.kstream.ValueMapper
import java.net.http.HttpResponse

internal class HendelseToSkattResponseMapper(private val skattClient: SkattClient) : ValueMapper<Hendelse, HttpResponse<String>> {

    override fun apply(hendelse: Hendelse): HttpResponse<String> {
        val request = skattClient.createGetRequest(pgiQueryParams(hendelse))
        return skattClient.getPensjonsgivendeInntekter(request, HttpResponse.BodyHandlers.ofString())
    }

    private fun pgiQueryParams(hendelse: Hendelse) =
            mapOf("identifikator" to hendelse.getIdentifikator(), "inntektsAar" to hendelse.getInntektsAar())
}