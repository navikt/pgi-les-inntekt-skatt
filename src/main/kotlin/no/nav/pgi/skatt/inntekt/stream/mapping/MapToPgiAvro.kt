package no.nav.pgi.skatt.inntekt.stream.mapping

import no.nav.pgi.skatt.inntekt.PgiDto
import no.nav.pgi.skatt.inntekt.PgiPerOrdningDto
import no.nav.samordning.pgi.schema.PensjonsgivendeInntekt
import no.nav.samordning.pgi.schema.PensjonsgivendeInntektPerOrdning
import no.nav.samordning.pgi.schema.Skatteordning
import org.apache.kafka.streams.kstream.ValueMapper

private const val FASTLAND = "FASTLAND"
private const val SVALBARD = "SVALBARD"
private const val KILDESKATT_PAA_LOENN = "KILDESKATT_PAA_LOENN"

internal class MapToPgiAvro : ValueMapper<PgiDto, PensjonsgivendeInntekt> {

    override fun apply(pgiDto: PgiDto): PensjonsgivendeInntekt = toPensjonsgivendeInntekt(pgiDto)

    private fun toPensjonsgivendeInntekt(pgiDto: PgiDto): PensjonsgivendeInntekt = PensjonsgivendeInntekt(
            pgiDto.norskPersonidentifikator,
            pgiDto.inntektsaar,
            toPensjonsgivendeInntektPerOrdning(pgiDto.pensjonsgivendeInntekt))

    private fun toPensjonsgivendeInntektPerOrdning(pensjonsgivendeInntekt: List<PgiPerOrdningDto>) =
            pensjonsgivendeInntekt.map {
                PensjonsgivendeInntektPerOrdning(
                        toSkatteordningEnum(it.skatteordning),
                        it.datoForFastsetting,
                        it.pensjonsgivendeInntektAvLoennsinntekt,
                        it.pensjonsgivendeInntektAvLoennsinntektBarePensjonsdel,
                        it.pensjonsgivendeInntektAvNaeringsinntekt,
                        it.pensjonsgivendeInntektAvNaeringsinntektFraFiskeFangstEllerFamiliebarnehage
                )
            }

    private fun toSkatteordningEnum(skatteordning: String?): Skatteordning =
            when (skatteordning) {
                FASTLAND -> Skatteordning.FASTLAND
                SVALBARD -> Skatteordning.SVALBARD
                KILDESKATT_PAA_LOENN -> Skatteordning.KILDESKATT_PAA_LOENN
                else -> throw MissingSkatteordningException(skatteordning)
            }
}

internal class MissingSkatteordningException(missingSkatteordning: String?) :
        Exception("""Cant find $missingSkatteordning in ${Skatteordning::class.simpleName} enum when converting from DTO to avro. """)
