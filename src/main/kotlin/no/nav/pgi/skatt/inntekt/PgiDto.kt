package no.nav.pgi.skatt.inntekt

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory

//TODO Diskuter DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES = false
private val logger = LoggerFactory.getLogger(PgiDto::class.java)
private val objectMapper = ObjectMapper().registerModule(KotlinModule()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

internal fun String.mapToPGIDto(): PgiDto = objectMapper.readValue<PgiDto>(this).also { it.validate() }

data class PgiDto(
        val norskPersonidentifikator: String?,
        val inntektsaar: Long?,
        val pensjonsgivendeInntekt: List<PgiPerOrdningDto> = emptyList()
) {
    @JsonIgnore
    internal fun validate() {
        if (norskPersonidentifikator == null) throw InntektDtoException("norskPersonidentifikator").also { logger.error(it.message) }
        if (inntektsaar == null) throw InntektDtoException("inntektsaar").also { logger.error(it.message) }
        pensjonsgivendeInntekt.forEach { it.validate() }
    }
}

data class PgiPerOrdningDto(
        val skatteordning: String?,
        val datoForFastetting: String?,
        val pensjonsgivendeInntektAvLoennsinntekt: Long?,
        val pensjonsgivendeInntektAvLoennsinntektBarePensjonsdel: Long?,
        val pensjonsgivendeInntektAvNaeringsinntekt: Long?,
        val pensjonsgivendeInntektAvNaeringsinntektFraFiskeFangstEllerFamiliebarnehage: Long?
) {
    @JsonIgnore
    internal fun validate() {
        if (skatteordning == null) throw InntektPerOrdningDtoException("skatteordning").also { logger.error(it.message) }
        if (datoForFastetting == null) throw InntektPerOrdningDtoException("datoForFastetting").also { logger.error(it.message) }
    }
}

internal class InntektDtoException(missingVariableName: String) : Exception("""$missingVariableName is missing in ${PgiDto::class.simpleName}""")
internal class InntektPerOrdningDtoException(missingVariableName: String) : Exception("""$missingVariableName is missing in ${PgiPerOrdningDto::class.simpleName}""")