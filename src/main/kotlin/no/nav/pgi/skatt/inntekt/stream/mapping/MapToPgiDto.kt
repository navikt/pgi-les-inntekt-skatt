package no.nav.pgi.skatt.inntekt.stream.mapping

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import no.nav.pensjon.samhandling.maskfnr.maskFnr
import no.nav.pgi.skatt.inntekt.skatt.PgiDto
import no.nav.pgi.skatt.inntekt.skatt.mapToPGIDto
import org.apache.kafka.streams.kstream.ValueMapper

class MapToPgiDto : ValueMapper<String, PgiDto> {
    override fun apply(response: String): PgiDto = responseDto(response)

    private fun responseDto(responseBody: String): PgiDto {
        try {
            return responseBody.mapToPGIDto()
        } catch (e: UnrecognizedPropertyException) {
            throw InvalidJsonMappingException(e)
        }
    }
}

class InvalidJsonMappingException(exception: Exception) : RuntimeException(exception.message!!.maskFnr())