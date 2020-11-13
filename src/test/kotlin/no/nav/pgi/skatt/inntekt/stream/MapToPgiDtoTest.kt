package no.nav.pgi.skatt.inntekt.stream

import no.nav.pgi.skatt.inntekt.InntektDtoException
import no.nav.pgi.skatt.inntekt.InntektPerOrdningDtoException
import no.nav.pgi.skatt.inntekt.PgiDto
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
    fun `should map all fields from string response to dto`() {
        val pgiResponse = createPgiResponse(pensjonsgivendeInntekt = listOf(
                createInntektPerSakatteordning(skatteordning = SKATTEORDNING_FASTLAND),
                createInntektPerSakatteordning(skatteordning = SKATTEORDNING_KILDESKATT_PAA_LOENN),
                createInntektPerSakatteordning(skatteordning = SKATTEORDNING_SVALBARD)
        ))
        val pgiDto: PgiDto = mapper.apply(pgiResponse)

        assertEquals(FNR, pgiDto.norskPersonidentifikator)
        assertEquals(INNTEKTS_AAR, pgiDto.inntektsaar)
        assertEquals(3, pgiDto.pensjonsgivendeInntekt.size)

        val fastland = pgiDto.getPgiDtoPerOrdning(SKATTEORDNING_FASTLAND)
        val kildeskattPaaLoenn = pgiDto.getPgiDtoPerOrdning(SKATTEORDNING_KILDESKATT_PAA_LOENN)
        val svalbard = pgiDto.getPgiDtoPerOrdning(SKATTEORDNING_SVALBARD)

        assertEquals(SKATTEORDNING_FASTLAND, fastland!!.skatteordning)
        assertEquals(DATO_FOR_FASTSETTING, fastland.datoForFastetting)
        assertEquals(INNTEKT_AV_LOENNSINNTEKT, fastland.pensjonsgivendeInntektAvLoennsinntekt)
        assertEquals(INNTEKT_AV_LOENNSINNTEKT_BARE_PENSJONSDEL, fastland.pensjonsgivendeInntektAvLoennsinntektBarePensjonsdel)
        assertEquals(INNTEKT_AV_NAERINGSINNTEKT, fastland.pensjonsgivendeInntektAvNaeringsinntekt)
        assertEquals(INNTEKT_AV_NAERINGSINNTEKT_FRA_FISKE_FANGST_ELLER_FAMILIEBARNEHAGE, fastland.pensjonsgivendeInntektAvNaeringsinntektFraFiskeFangstEllerFamiliebarnehage)

        assertEquals(SKATTEORDNING_KILDESKATT_PAA_LOENN, kildeskattPaaLoenn!!.skatteordning)
        assertEquals(SKATTEORDNING_SVALBARD, svalbard!!.skatteordning)
    }

    @Test
    fun `throws required parameter is missing InntektDtoException when fnr is missing`() {
        val response = """
            {
              "inntektsaar": "$INNTEKTS_AAR",
              "pensjonsgivendeInntekt": []
            }
        """

        assertThrows<InntektDtoException> { mapper.apply(response) }
    }

    @Test
    fun `throws InntektDtoException when fnr is null`() {
        assertThrows<InntektDtoException> { mapper.apply(createPgiResponse(norskPersonidentifikator = null)) }
    }

    @Test
    fun `throws InntektDtoException when inntektsaar is null`() {
        assertThrows<InntektDtoException> { mapper.apply(createPgiResponse(inntektsaar = null)) }
    }

    @Test
    fun `throws InntektPerOrdningDtoException when skatteordning is null`() {
        val pgiResponse = createPgiResponse(pensjonsgivendeInntekt = listOf(
                createInntektPerSakatteordning(skatteordning = null)
        ))

        assertThrows<InntektPerOrdningDtoException> { mapper.apply(pgiResponse) }
    }

    @Test
    fun `throws InntektPerOrdningDtoException when datoForFastetting is null`() {
        val pgiResponse = createPgiResponse(pensjonsgivendeInntekt = listOf(
                createInntektPerSakatteordning(datoForFastetting = null)
        ))

        assertThrows<InntektPerOrdningDtoException> { mapper.apply(pgiResponse) }
    }

    private fun createPgiResponse(norskPersonidentifikator: String? = FNR, inntektsaar: Long? = INNTEKTS_AAR, pensjonsgivendeInntekt: List<String> = emptyList()) =
            """{
                "norskPersonidentifikator": ${if (norskPersonidentifikator == null) null else """"$norskPersonidentifikator""""},
                "inntektsaar": $inntektsaar,
                "pensjonsgivendeInntekt": ${pensjonsgivendeInntekt.joinToString(",", "[", "]")}
            }"""


    private fun createInntektPerSakatteordning(
            skatteordning: String? = SKATTEORDNING_FASTLAND,
            datoForFastetting: String? = DATO_FOR_FASTSETTING,
            pensjonsgivendeInntektAvLoennsinntekt: Long = INNTEKT_AV_LOENNSINNTEKT,
            pensjonsgivendeInntektAvLoennsinntektBarePensjonsdel: Long = INNTEKT_AV_LOENNSINNTEKT_BARE_PENSJONSDEL,
            pensjonsgivendeInntektAvNaeringsinntekt: Long = INNTEKT_AV_NAERINGSINNTEKT,
            pensjonsgivendeInntektAvNaeringsinntektFraFiskeFangstEllerFamiliebarnehage: Long = INNTEKT_AV_NAERINGSINNTEKT_FRA_FISKE_FANGST_ELLER_FAMILIEBARNEHAGE) = """{
                "skatteordning": ${if (skatteordning == null) null else """"$skatteordning""""},
                "datoForFastetting": ${if (datoForFastetting == null) null else """"$datoForFastetting""""},
                "pensjonsgivendeInntektAvLoennsinntekt": $pensjonsgivendeInntektAvLoennsinntekt,
                "pensjonsgivendeInntektAvLoennsinntektBarePensjonsdel": $pensjonsgivendeInntektAvLoennsinntektBarePensjonsdel,
                "pensjonsgivendeInntektAvNaeringsinntekt": $pensjonsgivendeInntektAvNaeringsinntekt,
                "pensjonsgivendeInntektAvNaeringsinntektFraFiskeFangstEllerFamiliebarnehage": $pensjonsgivendeInntektAvNaeringsinntektFraFiskeFangstEllerFamiliebarnehage
            }"""

    private fun PgiDto.getPgiDtoPerOrdning(skatteordning: String) = pensjonsgivendeInntekt.find { it.skatteordning == skatteordning }
}



