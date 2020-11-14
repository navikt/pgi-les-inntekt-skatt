package no.nav.pgi.skatt.inntekt.stream

import no.nav.pgi.skatt.inntekt.PgiDto
import no.nav.pgi.skatt.inntekt.PgiPerOrdningDto
import no.nav.pgi.skatt.inntekt.stream.mapping.MapToPgiAvro
import no.nav.pgi.skatt.inntekt.stream.mapping.MissingSkatteordningException
import no.nav.samordning.pgi.schema.PensjonsgivendeInntekt
import org.apache.kafka.streams.kstream.ValueMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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


internal class MapToPgiAvroTest {

    private val mapper: ValueMapper<PgiDto, PensjonsgivendeInntekt> = MapToPgiAvro()

    @Test
    fun `should map fnr and inntektsaar from pgi to avro`() {
        val pensjonsgivendeInntekt: PensjonsgivendeInntekt = mapper.apply(createPgiDto(pensjonsgivendeInntekt = emptyList()))

        assertEquals(FNR, pensjonsgivendeInntekt.getNorskPersonidentifikator())
        assertEquals(INNTEKTS_AAR, pensjonsgivendeInntekt.getInntektsaar())
        assertTrue(pensjonsgivendeInntekt.getPensjonsgivendeInntekt().isEmpty())
    }

    @Test
    fun `should map PgiPerOrdningDto to avro`() {
        val pensjonsgivendeInntekt = mapper.apply(createPgiDto())

        val fastlandInntekt = pensjonsgivendeInntekt!!.getPgiPerOrdning(SKATTEORDNING_FASTLAND)
        val kildeskattPaaLoennInntekt = pensjonsgivendeInntekt.getPgiPerOrdning(SKATTEORDNING_KILDESKATT_PAA_LOENN)
        val svalbardInntekt = pensjonsgivendeInntekt.getPgiPerOrdning(SKATTEORDNING_SVALBARD)

        assertEquals(SKATTEORDNING_FASTLAND, fastlandInntekt!!.getSkatteordning().name)
        assertEquals(DATO_FOR_FASTSETTING, fastlandInntekt.getDatoForFastsetting())
        assertEquals(INNTEKT_AV_LOENNSINNTEKT, fastlandInntekt.getPensjonsgivendeInntektAvLoennsinntekt())
        assertEquals(INNTEKT_AV_LOENNSINNTEKT_BARE_PENSJONSDEL, fastlandInntekt.getPensjonsgivendeInntektAvLoennsinntektBarePensjonsdel())
        assertEquals(INNTEKT_AV_NAERINGSINNTEKT, fastlandInntekt.getPensjonsgivendeInntektAvNaeringsinntekt())
        assertEquals(INNTEKT_AV_NAERINGSINNTEKT_FRA_FISKE_FANGST_ELLER_FAMILIEBARNEHAGE, fastlandInntekt.getPensjonsgivendeInntektAvNaeringsinntektFraFiskeFangstEllerFamiliebarnehage())

        assertEquals(SKATTEORDNING_KILDESKATT_PAA_LOENN, kildeskattPaaLoennInntekt!!.getSkatteordning().name)
        assertEquals(SKATTEORDNING_SVALBARD, svalbardInntekt!!.getSkatteordning().name)
    }

    @Test
    fun `Throws MissingSkatteordningException when skatteordning string does not exist in skatteordning enum`() {
        assertThrows<MissingSkatteordningException> { mapper.apply(createPgiDto(pensjonsgivendeInntekt = listOf(createPgiPerOrdningDto(skatteordning = "FailingHard")))) }
    }

    private fun createPgiDto(
            norskPersonidentifikator: String = FNR,
            inntektsaar: Long = INNTEKTS_AAR,
            pensjonsgivendeInntekt: List<PgiPerOrdningDto> =
                    listOf(
                            createPgiPerOrdningDto(skatteordning = SKATTEORDNING_FASTLAND),
                            createPgiPerOrdningDto(skatteordning = SKATTEORDNING_KILDESKATT_PAA_LOENN),
                            createPgiPerOrdningDto(skatteordning = SKATTEORDNING_SVALBARD)
                    )
    ) = PgiDto(norskPersonidentifikator, inntektsaar, pensjonsgivendeInntekt)

    private fun createPgiPerOrdningDto(
            skatteordning: String = SKATTEORDNING_FASTLAND,
            datoForFastetting: String = DATO_FOR_FASTSETTING,
            pensjonsgivendeInntektAvLoennsinntekt: Long = INNTEKT_AV_LOENNSINNTEKT,
            pensjonsgivendeInntektAvLoennsinntektBarePensjonsdel: Long = INNTEKT_AV_LOENNSINNTEKT_BARE_PENSJONSDEL,
            pensjonsgivendeInntektAvNaeringsinntekt: Long = INNTEKT_AV_NAERINGSINNTEKT,
            pensjonsgivendeInntektAvNaeringsinntektFraFiskeFangstEllerFamiliebarnehage: Long = INNTEKT_AV_NAERINGSINNTEKT_FRA_FISKE_FANGST_ELLER_FAMILIEBARNEHAGE,
    ) = PgiPerOrdningDto(skatteordning,
            datoForFastetting,
            pensjonsgivendeInntektAvLoennsinntekt,
            pensjonsgivendeInntektAvLoennsinntektBarePensjonsdel,
            pensjonsgivendeInntektAvNaeringsinntekt,
            pensjonsgivendeInntektAvNaeringsinntektFraFiskeFangstEllerFamiliebarnehage)

}

private fun PensjonsgivendeInntekt.getPgiPerOrdning(skatteordning: String) =
        getPensjonsgivendeInntekt().find { it.getSkatteordning().name == skatteordning }

