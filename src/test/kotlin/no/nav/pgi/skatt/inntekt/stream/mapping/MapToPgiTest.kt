package no.nav.pgi.skatt.inntekt.stream.mapping

import io.mockk.every
import io.mockk.mockk
import no.nav.pgi.domain.PensjonsgivendeInntekt
import no.nav.pgi.domain.PensjonsgivendeInntektMetadata
import no.nav.pgi.skatt.inntekt.skatt.InntektDtoException
import no.nav.pgi.skatt.inntekt.skatt.InntektPerOrdningDtoException
import no.nav.pgi.skatt.inntekt.skatt.InvalidJsonMappingException
import org.apache.kafka.streams.kstream.ValueMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.http.HttpResponse

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

internal class MapToPgiTest {

    private val mapper: ValueMapper<PgiResponse, PensjonsgivendeInntekt> = MapToPgi()

    @Test
    fun `Should map fnr and inntektsaar from PgiResponse to avro`() {
        val pgiResponse = createPgiResponse(pensjonsgivendeInntekt = emptyList())
        val pensjonsgivendeInntekt: PensjonsgivendeInntekt = mapper.apply(pgiResponse)

        assertEquals(FNR, pensjonsgivendeInntekt.norskPersonidentifikator)
        assertEquals(INNTEKTS_AAR, pensjonsgivendeInntekt.inntektsaar)
        assertTrue(pensjonsgivendeInntekt.pensjonsgivendeInntekt.isEmpty())
    }

    @Test
    fun `Should map inntekt per skatteordning to avro`() {
        val pgiResponse = createPgiResponse(
            pensjonsgivendeInntekt = listOf(
                createInntektPerSkatteordning(skatteordning = SKATTEORDNING_FASTLAND),
                createInntektPerSkatteordning(skatteordning = SKATTEORDNING_KILDESKATT_PAA_LOENN),
                createInntektPerSkatteordning(skatteordning = SKATTEORDNING_SVALBARD)
            )
        )

        val pensjonsgivendeInntekt = mapper.apply(pgiResponse)

        val fastlandInntekt = pensjonsgivendeInntekt.getPgiPerOrdning(SKATTEORDNING_FASTLAND)
        val kildeskattPaaLoennInntekt = pensjonsgivendeInntekt.getPgiPerOrdning(SKATTEORDNING_KILDESKATT_PAA_LOENN)
        val svalbardInntekt = pensjonsgivendeInntekt.getPgiPerOrdning(SKATTEORDNING_SVALBARD)

        assertEquals(3, pensjonsgivendeInntekt.pensjonsgivendeInntekt.size)

        assertEquals(DATO_FOR_FASTSETTING, fastlandInntekt.datoForFastsetting)
        assertEquals(INNTEKT_AV_LOENNSINNTEKT, fastlandInntekt.pensjonsgivendeInntektAvLoennsinntekt)
        assertEquals(
            INNTEKT_AV_LOENNSINNTEKT_BARE_PENSJONSDEL,
            fastlandInntekt.pensjonsgivendeInntektAvLoennsinntektBarePensjonsdel
        )
        assertEquals(INNTEKT_AV_NAERINGSINNTEKT, fastlandInntekt.pensjonsgivendeInntektAvNaeringsinntekt)
        assertEquals(
            INNTEKT_AV_NAERINGSINNTEKT_FRA_FISKE_FANGST_ELLER_FAMILIEBARNEHAGE,
            fastlandInntekt.pensjonsgivendeInntektAvNaeringsinntektFraFiskeFangstEllerFamiliebarnehage
        )

        assertEquals(SKATTEORDNING_FASTLAND, fastlandInntekt.skatteordning.name)
        assertEquals(SKATTEORDNING_KILDESKATT_PAA_LOENN, kildeskattPaaLoennInntekt.skatteordning.name)
        assertEquals(SKATTEORDNING_SVALBARD, svalbardInntekt.skatteordning.name)
    }

    @Test
    fun `Should map values when pgiResponse contains null values`() {
        val pgiResponse = createPgiResponse(
            pensjonsgivendeInntekt = listOf(
                createInntektPerSkatteordning(
                    skatteordning = SKATTEORDNING_FASTLAND,
                    pensjonsgivendeInntektAvLoennsinntekt = null,
                    pensjonsgivendeInntektAvLoennsinntektBarePensjonsdel = null,
                    pensjonsgivendeInntektAvNaeringsinntekt = null,
                    pensjonsgivendeInntektAvNaeringsinntektFraFiskeFangstEllerFamiliebarnehage = null
                )
            )
        )
        val pensjonsgivendeInntekt = mapper.apply(pgiResponse)
        val fastland = pensjonsgivendeInntekt.getPgiPerOrdning(SKATTEORDNING_FASTLAND)

        assertEquals(FNR, pensjonsgivendeInntekt.norskPersonidentifikator)
        assertEquals(INNTEKTS_AAR, pensjonsgivendeInntekt.inntektsaar)
        assertEquals(1, pensjonsgivendeInntekt.pensjonsgivendeInntekt.size)
        assertEquals(SKATTEORDNING_FASTLAND, fastland.skatteordning.name)
        assertEquals(DATO_FOR_FASTSETTING, fastland.datoForFastsetting)
    }

    @Test
    fun `Should add metadata to avro`() {
        val retries = 1L
        val sekvensnummer = 2L

        val pgiResponse = createPgiResponse(metadata = PensjonsgivendeInntektMetadata(retries, sekvensnummer))
        val pensjonsgivendeInntekt: PensjonsgivendeInntekt = mapper.apply(pgiResponse)

        assertEquals(retries, pensjonsgivendeInntekt.metaData.retries)
        assertEquals(sekvensnummer, pensjonsgivendeInntekt.metaData.sekvensnummer)
    }

    @Test
    fun `Throws InntektDtoException when fnr is missing`() {
        val body = """
            {
              "inntektsaar": "$INNTEKTS_AAR",
              "pensjonsgivendeInntekt": []
            }
        """
        val pgiResponse = PgiResponse(mockkHttpResponse(body), PensjonsgivendeInntektMetadata(0, 0))

        assertThrows<InntektDtoException> { mapper.apply(pgiResponse) }
    }

    @Test
    fun `Throws MissingSkatteordningException when skatteordning string does not exist in skatteordning enum`() {
        val pgiResponse =
            createPgiResponse(pensjonsgivendeInntekt = listOf(createInntektPerSkatteordning(skatteordning = "NotValidSkatteordning")))
        assertThrows<MissingSkatteordningException> { mapper.apply(pgiResponse) }
    }

    @Test
    fun `Throws InvalidJsonMappingException when pgiResponse contains unrecognized properties`() {
        val pgiResponse = createPgiResponse(extraValue = 10L, pensjonsgivendeInntekt = emptyList())
        assertThrows<InvalidJsonMappingException> { mapper.apply(pgiResponse) }
    }

    @Test
    fun `Throws InntektDtoException when fnr is null throw`() {
        assertThrows<InntektDtoException> { mapper.apply(createPgiResponse(norskPersonidentifikator = null)) }
    }

    @Test
    fun `Throws InntektDtoException when inntektsaar is null`() {
        assertThrows<InntektDtoException> { mapper.apply(createPgiResponse(inntektsaar = null)) }
    }

    @Test
    fun `Throws InntektPerOrdningDtoException when skatteordning is null`() {
        val pgiResponse =
            createPgiResponse(pensjonsgivendeInntekt = listOf(createInntektPerSkatteordning(skatteordning = null)))
        assertThrows<InntektPerOrdningDtoException> { mapper.apply(pgiResponse) }
    }

    @Test
    fun `Throws InntektPerOrdningDtoException when datoForFastsetting is null`() {
        val pgiResponse =
            createPgiResponse(pensjonsgivendeInntekt = listOf(createInntektPerSkatteordning(datoForFastsetting = null)))
        assertThrows<InntektPerOrdningDtoException> { mapper.apply(pgiResponse) }
    }

    private fun createPgiResponse(
        norskPersonidentifikator: String? = FNR,
        inntektsaar: Long? = INNTEKTS_AAR,
        pensjonsgivendeInntekt: List<String> = emptyList(),
        extraValue: Long? = null,
        metadata: PensjonsgivendeInntektMetadata = PensjonsgivendeInntektMetadata(0, 0)
    ): PgiResponse {
        val mockHttpResponse = mockkHttpResponse(
            createPgiResponseBody(
                norskPersonidentifikator,
                inntektsaar,
                pensjonsgivendeInntekt,
                extraValue
            )
        )
        return PgiResponse(mockHttpResponse, metadata)
    }

    private fun mockkHttpResponse(body: String): HttpResponse<String> {
        val httpResponseMock = mockk<HttpResponse<String>>()
        every { httpResponseMock.hint(String::class).body() } returns body
        return httpResponseMock
    }

    private fun createPgiResponseBody(
        norskPersonidentifikator: String? = FNR,
        inntektsaar: Long? = INNTEKTS_AAR,
        pensjonsgivendeInntekt: List<String> = emptyList(),
        extraValue: Long? = null,
    ) = """{
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
        pensjonsgivendeInntektAvNaeringsinntektFraFiskeFangstEllerFamiliebarnehage: Long? = INNTEKT_AV_NAERINGSINNTEKT_FRA_FISKE_FANGST_ELLER_FAMILIEBARNEHAGE
    ) = """{
                "skatteordning": ${if (skatteordning == null) null else """"$skatteordning""""},
                "datoForFastsetting": ${if (datoForFastsetting == null) null else """"$datoForFastsetting""""},
                "pensjonsgivendeInntektAvLoennsinntekt": $pensjonsgivendeInntektAvLoennsinntekt,
                "pensjonsgivendeInntektAvLoennsinntektBarePensjonsdel": $pensjonsgivendeInntektAvLoennsinntektBarePensjonsdel,
                "pensjonsgivendeInntektAvNaeringsinntekt": $pensjonsgivendeInntektAvNaeringsinntekt,
                "pensjonsgivendeInntektAvNaeringsinntektFraFiskeFangstEllerFamiliebarnehage": $pensjonsgivendeInntektAvNaeringsinntektFraFiskeFangstEllerFamiliebarnehage
            }"""

    private fun PensjonsgivendeInntekt.getPgiPerOrdning(skatteordning: String) =
        pensjonsgivendeInntekt.find { it.skatteordning.name == skatteordning }!!
}
