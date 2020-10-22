package no.nav.pgi.skatt.inntekt.stream

import no.nav.samordning.pgi.schema.PensjonsgivendeInntekt
import org.apache.kafka.streams.kstream.ValueMapper
import java.net.http.HttpResponse

class PensjonsgivendeInntektMapper : ValueMapper<HttpResponse<String>, PensjonsgivendeInntekt> {
    override fun apply(response: HttpResponse<String>): PensjonsgivendeInntekt {
        return responseToPgi(response.body())
    }

    fun responseToPgi(responseBody: String): PensjonsgivendeInntekt {
        //string response body to pensjonsgivendeInntekt. PgiDto?
        return PensjonsgivendeInntekt("12345678901", "2020")
    }

}
