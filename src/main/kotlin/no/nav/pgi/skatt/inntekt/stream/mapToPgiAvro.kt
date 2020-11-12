package no.nav.pgi.skatt.inntekt.stream

import no.nav.pgi.skatt.inntekt.PgiDto
import no.nav.pgi.skatt.inntekt.PgiPerOrdningDto
import no.nav.samordning.pgi.schema.PensjonsgivendeInntekt
import no.nav.samordning.pgi.schema.PensjonsgivendeInntektPerOrdning
import no.nav.samordning.pgi.schema.Skatteordning
import org.apache.kafka.streams.kstream.ValueMapper

class mapToPgiAvro : ValueMapper<PgiDto, PensjonsgivendeInntekt> {
    override fun apply(PgiDto: PgiDto): PensjonsgivendeInntekt {
        return dtoToPensjonsgivendeInntekt(PgiDto)
    }

    internal fun dtoToPensjonsgivendeInntekt(pgiDto: PgiDto): PensjonsgivendeInntekt {
        return PensjonsgivendeInntekt(
                pgiDto.norskPersonidentifikator,
                pgiDto.inntektsaar.toString(),
                mapToPensjonsgivendeInntektPerOrdning(pgiDto.pensjonsgivendeInntekt))
    }

    internal fun mapToPensjonsgivendeInntektPerOrdning(pensjonsgivendeInntekt: List<PgiPerOrdningDto>) =
            pensjonsgivendeInntekt.map {
                PensjonsgivendeInntektPerOrdning(
                        mapToSkatteordningEnum(it.skatteordning),
                        it.datoForFastetting,
                        it.pensjonsgivendeInntektAvLoennsinntekt,
                        it.pensjonsgivendeInntektAvLoennsinntektBarePensjonsdel,
                        it.pensjonsgivendeInntektAvNaeringsinntekt,
                        it.pensjonsgivendeInntektAvNaeringsinntektFraFiskeFangstEllerFamiliebarnehage
                )
            }

    private fun mapToSkatteordningEnum(skatteordning: String?): Skatteordning =
            when (skatteordning) {
                "FASTLAND" -> Skatteordning.FASTLAND
                "SVALBARD" -> Skatteordning.SVALBARD
                "KILDESKATT_PAA_LOENN" -> Skatteordning.KILDESKATT_PAA_LOENN
                else -> throw MissingSkatteordningException(skatteordning)
            }
}

internal class MissingSkatteordningException(MissingSkatteordning: String?) :
        Exception("""Cant find $MissingSkatteordning in ${Skatteordning::class.simpleName} enum when converting from DTO to avro. """)
