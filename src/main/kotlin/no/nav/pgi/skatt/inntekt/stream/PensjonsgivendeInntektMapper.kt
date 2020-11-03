package no.nav.pgi.skatt.inntekt.stream

import no.nav.pgi.skatt.inntekt.mapToPensjonsgivendeInntektDtoDto
import no.nav.samordning.pgi.schema.PensjonsgivendeInntekt
import org.apache.kafka.streams.kstream.ValueMapper
import java.net.http.HttpResponse

class PensjonsgivendeInntektMapper : ValueMapper<HttpResponse<String>, PensjonsgivendeInntekt> {
    override fun apply(response: HttpResponse<String>): PensjonsgivendeInntekt {
        return responseToPgi(response.body())
    }

    //TODO lag avro og mapper til kafka inntekt. Eventuelt lage en egen mapper for denne transformasjonen
    fun responseToPgi(responseBody: String): PensjonsgivendeInntekt {
        val dto = responseBody.mapToPensjonsgivendeInntektDtoDto()
        return PensjonsgivendeInntekt(dto.norskPersonidentifikator, dto.inntektsaar.toString())
    }
}
