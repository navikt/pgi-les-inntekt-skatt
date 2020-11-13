package no.nav.pgi.skatt.inntekt.stream

import no.nav.pgi.skatt.inntekt.PgiDto
import no.nav.pgi.skatt.inntekt.mapToPGIDto
import org.apache.kafka.streams.kstream.ValueMapper

class MapToPgiDto : ValueMapper<String, PgiDto> {
    override fun apply(response: String): PgiDto = responseDto(response)

    private fun responseDto(responseBody: String) = responseBody.mapToPGIDto()
}
