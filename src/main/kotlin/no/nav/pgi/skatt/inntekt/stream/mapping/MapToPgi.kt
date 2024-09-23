package no.nav.pgi.skatt.inntekt.stream.mapping

import no.nav.pgi.domain.PensjonsgivendeInntekt
import no.nav.pgi.domain.PensjonsgivendeInntektPerOrdning
import no.nav.pgi.domain.Skatteordning
import no.nav.pgi.skatt.inntekt.skatt.PgiPerOrdningDto
import no.nav.pgi.skatt.inntekt.skatt.mapToPGIDto
import org.apache.kafka.streams.kstream.ValueMapper

private const val FASTLAND = "FASTLAND"
private const val SVALBARD = "SVALBARD"
private const val KILDESKATT_PAA_LOENN = "KILDESKATT_PAA_LOENN"

internal class MapToPgi : ValueMapper<PgiResponse, PensjonsgivendeInntekt> {

    override fun apply(pgiResponse: PgiResponse): PensjonsgivendeInntekt {
        val pgiDto = pgiResponse.mapToPGIDto()
        return PensjonsgivendeInntekt(
            pgiDto.norskPersonidentifikator!!, // TODO: !!
            pgiDto.inntektsaar!!,
            toPensjonsgivendeInntektPerOrdning(pgiDto.pensjonsgivendeInntekt),
            pgiResponse.metadata()
        )
    }

    private fun toPensjonsgivendeInntektPerOrdning(pensjonsgivendeInntekt: List<PgiPerOrdningDto>) =
        pensjonsgivendeInntekt.map {
            PensjonsgivendeInntektPerOrdning(
                toSkatteordningEnum(it.skatteordning),
                it.datoForFastsetting!!, // TODO: !!
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
