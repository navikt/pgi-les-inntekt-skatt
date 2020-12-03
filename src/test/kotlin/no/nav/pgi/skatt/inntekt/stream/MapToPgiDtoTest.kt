package no.nav.pgi.skatt.inntekt.stream

import no.nav.pgi.skatt.inntekt.skatt.InntektDtoException
import no.nav.pgi.skatt.inntekt.skatt.InntektPerOrdningDtoException
import no.nav.pgi.skatt.inntekt.skatt.PgiDto
import no.nav.pgi.skatt.inntekt.stream.mapping.InvalidJsonMappingException
import no.nav.pgi.skatt.inntekt.stream.mapping.MapToPgiDto
import org.apache.kafka.streams.kstream.ValueMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

private const val FNR = "12345678901"
private const val INNTEKTS_AAR = 2018L

private const val SKATTEORDNING_FASTLAND = "FASTLAND"
private const val SKATTEORDNING_KILDESKATT_PAA_LOENN = "KILDESKATT_PAA_LOENN"
private const val SKATTEORDNING_SVALBARD = "SVALBARD"
private const val DATO_FOR_FASTSETTING = "2018-01-01"
private const val INNTEKT_AV_LOENNSINNTEKT = 1L
private const val INNTEKT_AV_LOENNSINNTEKT_BARE_PENSJONSDEL = 2L
private const val INNTEKT_AV_NAERINGSINNTEKT = 3L
private const val INNTEKT_AV_NAERINGSINNTEKT_FRA_FISKE_FANGST_ELLER_FAMILIEBARNEHAGE = 4L

internal class MapToPgiDtoTest {

    private val mapper: ValueMapper<String, PgiDto> = MapToPgiDto()

    @Test
    fun `when all the correct values are returned from skatt all correct values are mapped into the dto object`() {
        val pgiResponse = createPgiResponse(pensjonsgivendeInntekt = listOf(
                createInntektPerSkatteordning(skatteordning = SKATTEORDNING_FASTLAND),
                createInntektPerSkatteordning(skatteordning = SKATTEORDNING_KILDESKATT_PAA_LOENN),
                createInntektPerSkatteordning(skatteordning = SKATTEORDNING_SVALBARD)
        ))
        val pgiDto: PgiDto = mapper.apply(pgiResponse)
        val fastland = pgiDto.getPgiDtoPerOrdning(SKATTEORDNING_FASTLAND)
        val kildeskattPaaLoenn = pgiDto.getPgiDtoPerOrdning(SKATTEORDNING_KILDESKATT_PAA_LOENN)
        val svalbard = pgiDto.getPgiDtoPerOrdning(SKATTEORDNING_SVALBARD)

        assertEquals(FNR, pgiDto.norskPersonidentifikator)
        assertEquals(INNTEKTS_AAR, pgiDto.inntektsaar)
        assertEquals(3, pgiDto.pensjonsgivendeInntekt.size)

        assertEquals(SKATTEORDNING_FASTLAND, fastland!!.skatteordning)
        assertEquals(DATO_FOR_FASTSETTING, fastland.datoForFastsetting)
        assertEquals(INNTEKT_AV_LOENNSINNTEKT, fastland.pensjonsgivendeInntektAvLoennsinntekt)
        assertEquals(INNTEKT_AV_LOENNSINNTEKT_BARE_PENSJONSDEL, fastland.pensjonsgivendeInntektAvLoennsinntektBarePensjonsdel)
        assertEquals(INNTEKT_AV_NAERINGSINNTEKT, fastland.pensjonsgivendeInntektAvNaeringsinntekt)
        assertEquals(INNTEKT_AV_NAERINGSINNTEKT_FRA_FISKE_FANGST_ELLER_FAMILIEBARNEHAGE, fastland.pensjonsgivendeInntektAvNaeringsinntektFraFiskeFangstEllerFamiliebarnehage)

        assertEquals(SKATTEORDNING_KILDESKATT_PAA_LOENN, kildeskattPaaLoenn!!.skatteordning)
        assertEquals(SKATTEORDNING_SVALBARD, svalbard!!.skatteordning)
    }

    @Test
    fun `when response contains unrecognized properties then throw InvalidJsonMappingException`() {
        val pgiResponse = createPgiResponse(extraValue = 10L, pensjonsgivendeInntekt = emptyList())
        assertThrows<InvalidJsonMappingException> { mapper.apply(pgiResponse) }
    }

    @Test
    fun `when response contains subset of values then correct values are mapped`() {
        val pgiResponse = createPgiResponse(pensjonsgivendeInntekt = listOf(
                createInntektPerSkatteordning(
                        skatteordning = SKATTEORDNING_FASTLAND,
                        pensjonsgivendeInntektAvLoennsinntekt = null,
                        pensjonsgivendeInntektAvLoennsinntektBarePensjonsdel = null,
                        pensjonsgivendeInntektAvNaeringsinntekt = null,
                        pensjonsgivendeInntektAvNaeringsinntektFraFiskeFangstEllerFamiliebarnehage = null)
        ))
        val pgiDto: PgiDto = mapper.apply(pgiResponse)
        val fastland = pgiDto.getPgiDtoPerOrdning(SKATTEORDNING_FASTLAND)

        assertEquals(FNR, pgiDto.norskPersonidentifikator)
        assertEquals(INNTEKTS_AAR, pgiDto.inntektsaar)
        assertEquals(1, pgiDto.pensjonsgivendeInntekt.size)
        assertEquals(SKATTEORDNING_FASTLAND, fastland!!.skatteordning)
        assertEquals(DATO_FOR_FASTSETTING, fastland.datoForFastsetting)
    }

    @Test
    fun `when when fnr is missing throw InntektDtoException`() {
        val response = """
            {
              "inntektsaar": "$INNTEKTS_AAR",
              "pensjonsgivendeInntekt": []
            }
        """

        assertThrows<InntektDtoException> { mapper.apply(response) }
    }

    @Test
    fun `when fnr is null throw InntektDtoException`() {
        assertThrows<InntektDtoException> { mapper.apply(createPgiResponse(norskPersonidentifikator = null)) }
    }

    @Test
    fun `when inntektsaar is null throw InntektDtoException`() {
        assertThrows<InntektDtoException> { mapper.apply(createPgiResponse(inntektsaar = null)) }
    }

    @Test
    fun `when skatteordning is null throw InntektPerOrdningDtoException`() {
        val pgiResponse = createPgiResponse(pensjonsgivendeInntekt = listOf(createInntektPerSkatteordning(skatteordning = null)))

        assertThrows<InntektPerOrdningDtoException> { mapper.apply(pgiResponse) }
    }

    @Test
    fun `datoForFastsetting is null throw InntektPerOrdningDtoException`() {
        val pgiResponse = createPgiResponse(pensjonsgivendeInntekt = listOf(createInntektPerSkatteordning(datoForFastsetting = null)))

        assertThrows<InntektPerOrdningDtoException> { mapper.apply(pgiResponse) }
    }

    private fun createPgiResponse(norskPersonidentifikator: String? = FNR, inntektsaar: Long? = INNTEKTS_AAR, pensjonsgivendeInntekt: List<String> = emptyList(), extraValue: Long? = null) =
            """{
                "norskPersonidentifikator": ${if (norskPersonidentifikator == null) null else """"$norskPersonidentifikator""""},
                "inntektsaar": $inntektsaar,
                "pensjonsgivendeInntekt": ${pensjonsgivendeInntekt.joinToString(",", "[", "]")}
                ${if (extraValue == null) "" else ""","extraValue": $extraValue """}
            }"""


    private fun createInntektPerSkatteordning(
            skatteordning: String? = SKATTEORDNING_FASTLAND,
            datoForFastsetting: String? = DATO_FOR_FASTSETTING,
            pensjonsgivendeInntektAvLoennsinntekt: Long? = INNTEKT_AV_LOENNSINNTEKT,
            pensjonsgivendeInntektAvLoennsinntektBarePensjonsdel: Long? = INNTEKT_AV_LOENNSINNTEKT_BARE_PENSJONSDEL,
            pensjonsgivendeInntektAvNaeringsinntekt: Long? = INNTEKT_AV_NAERINGSINNTEKT,
            pensjonsgivendeInntektAvNaeringsinntektFraFiskeFangstEllerFamiliebarnehage: Long? = INNTEKT_AV_NAERINGSINNTEKT_FRA_FISKE_FANGST_ELLER_FAMILIEBARNEHAGE) = """{
                "skatteordning": ${if (skatteordning == null) null else """"$skatteordning""""},
                "datoForFastsetting": ${if (datoForFastsetting == null) null else """"$datoForFastsetting""""},
                "pensjonsgivendeInntektAvLoennsinntekt": $pensjonsgivendeInntektAvLoennsinntekt,
                "pensjonsgivendeInntektAvLoennsinntektBarePensjonsdel": $pensjonsgivendeInntektAvLoennsinntektBarePensjonsdel,
                "pensjonsgivendeInntektAvNaeringsinntekt": $pensjonsgivendeInntektAvNaeringsinntekt,
                "pensjonsgivendeInntektAvNaeringsinntektFraFiskeFangstEllerFamiliebarnehage": $pensjonsgivendeInntektAvNaeringsinntektFraFiskeFangstEllerFamiliebarnehage
            }"""

    private fun PgiDto.getPgiDtoPerOrdning(skatteordning: String) = pensjonsgivendeInntekt.find { it.skatteordning == skatteordning }
}



