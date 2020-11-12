package no.nav.pgi.skatt.inntekt.stream

import no.nav.pgi.skatt.inntekt.PgiDto
import no.nav.pgi.skatt.inntekt.mapToPGIDto
import org.apache.kafka.streams.kstream.ValueMapper
import java.net.http.HttpResponse

class MapToPgiDto : ValueMapper<HttpResponse<String>, PgiDto> {
    override fun apply(response: HttpResponse<String>): PgiDto = responseDto(response.body())

    private fun responseDto(responseBody: String) = responseBody.mapToPGIDto()
}
