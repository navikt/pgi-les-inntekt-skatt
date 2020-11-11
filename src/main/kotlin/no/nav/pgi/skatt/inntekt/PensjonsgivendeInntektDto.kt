package no.nav.pgi.skatt.inntekt

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(PensjonsgivendeInntektDto::class.java)
private val objectMapper = ObjectMapper().registerModule(KotlinModule())

internal fun String.mapToPensjonsgivendeInntektDtoDto(): PensjonsgivendeInntektDto {
    return objectMapper.readValue<PensjonsgivendeInntektDto>(this)
            .also{it.validate()}
}

internal data class PensjonsgivendeInntektDto(
        val norskPersonidentifikator: String?,
        val inntektsaar: Int?,
        val pensjonsgivendeInntekt: List<PensjonsgivendeInntektPerOrdningDto> = emptyList()
) {
    @JsonIgnore
    internal fun validate() {
        if (norskPersonidentifikator == null) throw InntektDtoException("norskPersonidentifikator").also { logger.error(it.message) }
        if (inntektsaar == null) throw InntektDtoException("inntektsaar").also { logger.error(it.message) }
        pensjonsgivendeInntekt.forEach{it.validate()}
    }
}

internal data class PensjonsgivendeInntektPerOrdningDto(
        val skatteordning: String?,
        val datoForFastetting: String?,
        val pensjonsgivendeInntektAvLoennsinntekt: Int?,
        val pensjonsgivendeInntektAvLoennsinntektBarePensjonsdel: Int?,
        val pensjonsgivendeInntektAvNaeringsinntekt: Int?,
        val pensjonsgivendeInntektAvNaeringsinntektFraFiskeFangstEllerFamiliebarnehage: Int?,
) {
    @JsonIgnore
    internal fun validate() {
        if (skatteordning == null) throw inntektPerOrdningDtoException("skatteordning").also { logger.error(it.message) }
        if (datoForFastetting == null) throw inntektPerOrdningDtoException("datoForFastetting").also { logger.error(it.message) }
    }
}

internal class InntektDtoException(MissingVariableName: String) : Exception("""$MissingVariableName is missing in ${PensjonsgivendeInntektDto::class.simpleName}""")
internal class inntektPerOrdningDtoException(MissingVariableName: String) : Exception("""$MissingVariableName is missing in ${PensjonsgivendeInntektPerOrdningDto::class.simpleName}""")