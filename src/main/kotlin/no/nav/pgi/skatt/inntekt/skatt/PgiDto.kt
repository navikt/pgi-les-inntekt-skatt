package no.nav.pgi.skatt.inntekt.skatt

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.pensjon.samhandling.maskfnr.maskFnr
import no.nav.pgi.skatt.inntekt.stream.mapping.PgiResponse
import org.slf4j.LoggerFactory

private val LOG = LoggerFactory.getLogger(PgiDto::class.java)
private val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)

internal fun PgiResponse.mapToPGIDto(): PgiDto = try {
    objectMapper.readValue<PgiDto>(this.body())
        .also { it.validate(traceString()) }
} catch (e: UnrecognizedPropertyException) {
    throw InvalidJsonMappingException(e, traceString())
}

data class PgiDto(
    val norskPersonidentifikator: String?,
    val inntektsaar: Long?,
    val pensjonsgivendeInntekt: List<PgiPerOrdningDto> = emptyList()
) {
    @JsonIgnore
    internal fun validate(traceString: String) {
        if (norskPersonidentifikator == null) throw InntektDtoException("norskPersonidentifikator", traceString)
            .also { LOG.error(it.message) }
        if (inntektsaar == null) throw InntektDtoException("inntektsaar", traceString)
            .also { LOG.error(it.message) }
        pensjonsgivendeInntekt.forEach { it.validate(traceString) }
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
    internal fun validate(traceString: String) {
        if (skatteordning == null) throw InntektPerOrdningDtoException("skatteordning", traceString)
            .also { LOG.error(it.message) }
        if (datoForFastsetting == null) throw InntektPerOrdningDtoException("datoForFastsetting", traceString)
            .also { LOG.error(it.message) }
    }
}

internal class InntektDtoException(missingVariableName: String, traceString: String) :
    Exception("""$missingVariableName is missing in ${PgiDto::class.simpleName}. $traceString""")

internal class InntektPerOrdningDtoException(missingVariableName: String, traceString: String) :
    Exception("""$missingVariableName is missing in ${PgiPerOrdningDto::class.simpleName}. $traceString""")

internal class InvalidJsonMappingException(exception: Exception, traceString: String) :
    RuntimeException("""${exception.message!!.maskFnr()}. $traceString""")