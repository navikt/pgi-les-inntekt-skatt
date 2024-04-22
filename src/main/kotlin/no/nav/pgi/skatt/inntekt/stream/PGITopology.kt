package no.nav.pgi.skatt.inntekt.stream

import io.prometheus.client.Counter
import net.logstash.logback.marker.Markers
import no.nav.pensjon.samhandling.maskfnr.maskFnr
import no.nav.pgi.skatt.inntekt.skatt.PgiClient
import no.nav.pgi.skatt.inntekt.stream.mapping.FetchPgiFromSkatt
import no.nav.pgi.skatt.inntekt.stream.mapping.HandleErrorCodeFromSkatt
import no.nav.pgi.skatt.inntekt.stream.mapping.MapToPgiAvro
import no.nav.pgi.skatt.inntekt.stream.mapping.PgiResponse
import no.nav.samordning.pgi.schema.Hendelse
import no.nav.samordning.pgi.schema.HendelseKey
import no.nav.samordning.pgi.schema.PensjonsgivendeInntekt
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.KStream
import org.slf4j.LoggerFactory

private val hendelserToinntektProcessedTotal = Counter.build()
    .name("pgi_hendelse_to_inntekt_processed_total")
    .help("Antall hendelser hvor det er hentet inntekt totalt").register()
private val hendelserToinntektProcessedByYear = Counter.build()
    .name("pgi_hendelse_to_inntekt_processed_by_year")
    .labelNames("year")
    .help("Antall hendelser  hvor det er hentet inntekt per år").register()

internal class PGITopology(private val pgiClient: PgiClient = PgiClient()) {

    internal fun topology(): Topology {
        val streamBuilder = StreamsBuilder()
        val stream: KStream<HendelseKey, Hendelse> = streamBuilder.stream(PGI_HENDELSE_TOPIC)

        stream.peek(logHendelseAboutToBeProcessed())
            .mapValues(FetchPgiFromSkatt(pgiClient))
            .mapValues(HandleErrorCodeFromSkatt())
            .filter(pgiResponseNotNull())
            .mapValues(MapToPgiAvro())
            .peek(logAndCountInntektProcessed())
            .to(PGI_INNTEKT_TOPIC)

        return streamBuilder.build()
    }

    private fun logHendelseAboutToBeProcessed(): (HendelseKey, Hendelse) -> Unit =
        { _: HendelseKey, hendelse: Hendelse ->
            LOG.info(Markers.append("sekvensnummer", hendelse.getSekvensnummer().toString()),"""Started processing hendelse ${hendelse.toString().maskFnr()}""")
        }

    private fun pgiResponseNotNull(): (HendelseKey, PgiResponse?) -> Boolean = { _, pgiResponse -> pgiResponse != null }

    private fun logAndCountInntektProcessed(): (HendelseKey, PensjonsgivendeInntekt) -> Unit =
        { key: HendelseKey, pgi: PensjonsgivendeInntekt ->
            hendelserToinntektProcessedTotal.inc()
            hendelserToinntektProcessedByYear.labels(key.getGjelderPeriode()).inc()
            LOG.info(Markers.append("sekvensnummer", pgi.getMetaData().getSekvensnummer().toString()), "Lest inntekt Skatt: ${pgi.toString().maskFnr()}")
        }

    private companion object {
        private val LOG = LoggerFactory.getLogger(PGITopology::class.java)
    }
}

