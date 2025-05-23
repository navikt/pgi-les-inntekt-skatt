package no.nav.pgi.skatt.inntekt.skatt

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import net.logstash.logback.marker.Markers
import no.nav.pgi.skatt.inntekt.util.maskFnr
import no.nav.pgi.skatt.inntekt.stream.mapping.PgiResponse
import org.slf4j.LoggerFactory

private val LOG = LoggerFactory.getLogger(PgiDto::class.java)
private val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)

internal fun PgiResponse.mapToPGIDto(): PgiDto = try {
    objectMapper.readValue<PgiDto>(this.body())
        .also { it.validate(sekvensnummer()) }
} catch (e: UnrecognizedPropertyException) {
    throw InvalidJsonMappingException(e)
        .also { LOG.error(Markers.append("sekvensnummer", sekvensnummer()), it.message) }
}

data class PgiDto(
    val norskPersonidentifikator: String?,
    val inntektsaar: Long?,
    val pensjonsgivendeInntekt: List<PgiPerOrdningDto> = emptyList()
) {
    @JsonIgnore
    internal fun validate(sekvensnummer: Long) {
        if (norskPersonidentifikator == null) throw InntektDtoException("norskPersonidentifikator")
            .also { LOG.error(Markers.append("sekvensnummer", sekvensnummer), it.message) }
        if (inntektsaar == null) throw InntektDtoException("inntektsaar")
            .also { LOG.error(Markers.append("sekvensnummer", sekvensnummer), it.message) }
        pensjonsgivendeInntekt.forEach { it.validate(sekvensnummer) }
    }
}

data class PgiPerOrdningDto(
    val skatteordning: String?,
    val datoForFastsetting: String?,
    val pensjonsgivendeInntektAvLoennsinntekt: Long?,
    val pensjonsgivendeInntektAvLoennsinntektBarePensjonsdel: Long?,
    val pensjonsgivendeInntektAvNaeringsinntekt: Long?,
    val pensjonsgivendeInntektAvNaeringsinntektFraFiskeFangstEllerFamiliebarnehage: Long?
) {
    @JsonIgnore
    internal fun validate(sekvensnummer: Long) {
        if (skatteordning == null) throw InntektPerOrdningDtoException("skatteordning")
            .also { LOG.error(Markers.append("sekvensnummer", sekvensnummer), it.message) }
        if (datoForFastsetting == null) throw InntektPerOrdningDtoException("datoForFastsetting")
            .also { LOG.error(Markers.append("sekvensnummer", sekvensnummer), it.message) }
    }
}

internal class InntektDtoException(missingVariableName: String) :
    Exception("""$missingVariableName is missing in ${PgiDto::class.simpleName} """)

internal class InntektPerOrdningDtoException(missingVariableName: String) :
    Exception("""$missingVariableName is missing in ${PgiPerOrdningDto::class.simpleName} """)

internal class InvalidJsonMappingException(exception: Exception) :
    RuntimeException(exception.message!!.maskFnr())