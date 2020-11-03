package no.nav.pgi.skatt.inntekt.stream

import no.nav.pgi.skatt.inntekt.PensjonsgivendeInntektClient
import no.nav.samordning.pgi.schema.Hendelse
import org.apache.kafka.streams.kstream.ValueMapper
import java.net.http.HttpResponse

internal class HendelseToPensjonsgivendeInntektResponseMapper(private val pensjonsgivendeInntektClient: PensjonsgivendeInntektClient) : ValueMapper<Hendelse, HttpResponse<String>> {

    override fun apply(hendelse: Hendelse): HttpResponse<String> {
        val request = pensjonsgivendeInntektClient.createPensjonsgivendeInntekterRequest(hendelse.getInntektsAar(), hendelse.getIdentifikator())
        return pensjonsgivendeInntektClient.getPensjonsgivendeInntekter(request, HttpResponse.BodyHandlers.ofString())
    }
}