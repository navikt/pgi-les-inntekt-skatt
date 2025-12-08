package no.nav.pgi.skatt.inntekt.stream

import com.fasterxml.jackson.core.JacksonException
import net.logstash.logback.marker.Markers
import no.nav.pgi.skatt.inntekt.util.maskFnr
import no.nav.pgi.domain.Hendelse
import no.nav.pgi.domain.HendelseKey
import no.nav.pgi.domain.PensjonsgivendeInntekt
import no.nav.pgi.domain.serialization.PgiDomainSerializer
import no.nav.pgi.skatt.inntekt.Counters
import no.nav.pgi.skatt.inntekt.skatt.PgiClient
import no.nav.pgi.skatt.inntekt.stream.mapping.FetchPgiFromSkatt
import no.nav.pgi.skatt.inntekt.stream.mapping.HandleErrorCodeFromSkatt
import no.nav.pgi.skatt.inntekt.stream.mapping.MapToPgi
import no.nav.pgi.skatt.inntekt.stream.mapping.PgiResponse
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.KStream
import org.slf4j.Logger
import org.slf4j.LoggerFactory


internal class PGITopology(val counters: Counters, private val pgiClient: PgiClient = PgiClient()) {

    internal fun topology(): Topology {
        val streamBuilder = StreamsBuilder()
        val stream: KStream<String, String> = streamBuilder.stream(PGI_HENDELSE_TOPIC)

        stream
            .filter(kafkaHendelseParsable())
            .map { key, value ->
                KeyValue(
                    PgiDomainSerializer().fromJson(HendelseKey::class, key),
                    PgiDomainSerializer().fromJson(Hendelse::class, value)
                )
            }
            .peek(logHendelseAboutToBeProcessed())
            .mapValues(FetchPgiFromSkatt(pgiClient))
            .mapValues(HandleErrorCodeFromSkatt(counters = counters))
            .filter(pgiResponseNotNull())
            .mapValues(MapToPgi())
            .peek(logAndCountInntektProcessed())
            .map { key, value ->
                KeyValue(
                    PgiDomainSerializer().toJson(key),
                    PgiDomainSerializer().toJson(value)
                )
            }
            .to(PGI_INNTEKT_TOPIC)

        return streamBuilder.build()
    }

    private fun logHendelseAboutToBeProcessed(): (HendelseKey, Hendelse) -> Unit =
        { _: HendelseKey, hendelse: Hendelse ->
            LOG.info(
                Markers.append("sekvensnummer", hendelse.sekvensnummer.toString()),
                """Started processing hendelse ${hendelse.toString().maskFnr()}"""
            )
        }

    private fun pgiResponseNotNull(): (HendelseKey, PgiResponse?) -> Boolean = { _, pgiResponse -> pgiResponse != null }

    private fun kafkaHendelseParsable(): (String, String) -> Boolean =
        { key, value ->
            try {
                PgiDomainSerializer().fromJson(HendelseKey::class, key)
                PgiDomainSerializer().fromJson(Hendelse::class, value)
                true
            } catch (e: JacksonException) {
                LOG.error("Failed to deserialize kafka message, discarding it", e)
                false
            }
        }

    private fun logAndCountInntektProcessed(): (HendelseKey, PensjonsgivendeInntekt) -> Unit =
        { key: HendelseKey, pgi: PensjonsgivendeInntekt ->
            counters.increaseHendelserToinntektProcessedTotal()
            counters.increaseHendelserToInntektProcessedByYear(key.gjelderPeriode)
            val marker = Markers.append("sekvensnummer", pgi.metaData.sekvensnummer.toString())
            val sekvensnummer = pgi.metaData.sekvensnummer
            LOG.info(
                marker,
                "Lest inntekt Skatt: ${pgi.toString().maskFnr()}. Sekvensnummer: $sekvensnummer"
            )
            SECURE_LOG.info(
                marker,
                "Lest inntekt Skatt: ${pgi}. Sekvensnummer: $sekvensnummer"
            )
        }

    private companion object {
        private val LOG = LoggerFactory.getLogger(PGITopology::class.java)
        private val SECURE_LOG: Logger = LoggerFactory.getLogger("team")
    }
}

