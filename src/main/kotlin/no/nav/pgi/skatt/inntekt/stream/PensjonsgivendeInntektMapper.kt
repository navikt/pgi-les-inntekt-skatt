package no.nav.pgi.skatt.inntekt.stream

import no.nav.samordning.pgi.schema.Hendelse
import no.nav.samordning.pgi.schema.HendelseKey
import no.nav.samordning.pgi.schema.PensjonsgivendeInntekt
import org.apache.kafka.streams.kstream.ValueMapperWithKey

internal class PensjonsgivendeInntektMapper : ValueMapperWithKey<HendelseKey, Hendelse, PensjonsgivendeInntekt> {
    override fun apply(key: HendelseKey, value: Hendelse): PensjonsgivendeInntekt {
        return PensjonsgivendeInntekt("12345678901", "2020")
    }
}