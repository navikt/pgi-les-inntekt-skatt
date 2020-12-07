package no.nav.pgi.skatt.inntekt.stream

import no.nav.pensjon.samhandling.maskfnr.maskFnr
import no.nav.pgi.skatt.inntekt.skatt.PgiClient
import no.nav.pgi.skatt.inntekt.stream.mapping.FetchPgiFromSkatt
import no.nav.pgi.skatt.inntekt.stream.mapping.HandleErrorCodeFromSkatt
import no.nav.pgi.skatt.inntekt.stream.mapping.MapToPgiAvro
import no.nav.pgi.skatt.inntekt.stream.mapping.MapToPgiDto
import no.nav.samordning.pgi.schema.Hendelse
import no.nav.samordning.pgi.schema.HendelseKey
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.KStream
import org.slf4j.LoggerFactory

internal class PGITopology(private val pgiClient: PgiClient = PgiClient()) {

    internal fun topology(): Topology {
        val streamBuilder = StreamsBuilder()
        val stream: KStream<HendelseKey, Hendelse> = streamBuilder.stream(PGI_HENDELSE_TOPIC)

        stream.peek(logHendelseAboutToBeProcessed())
                .mapValues(FetchPgiFromSkatt(pgiClient))
                .mapValues(HandleErrorCodeFromSkatt())
                .mapValues(MapToPgiDto())
                .mapValues(MapToPgiAvro())
                .to(PGI_INNTEKT_TOPIC)

        return streamBuilder.build()
    }

    private fun logHendelseAboutToBeProcessed(): (HendelseKey, Hendelse) -> Unit =
            { _: HendelseKey, hendelse: Hendelse -> LOG.info("Started processing hendelse ${hendelse.toString().maskFnr()}") }


    private companion object {
        private val LOG = LoggerFactory.getLogger(PGITopology::class.java)
    }
}

