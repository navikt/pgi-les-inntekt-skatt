package no.nav.pgi.skatt.inntekt.stream.mapping

import net.logstash.logback.marker.Markers
import no.nav.pgi.skatt.inntekt.util.maskFnr
import no.nav.pgi.domain.Hendelse
import no.nav.pgi.domain.PensjonsgivendeInntektMetadata
import no.nav.pgi.skatt.inntekt.skatt.PgiClient
import org.apache.kafka.streams.kstream.ValueMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.http.HttpResponse

internal class FetchPgiFromSkatt(private val pgiClient: PgiClient) :
    ValueMapper<Hendelse, PgiResponse> {

    override fun apply(hendelse: Hendelse): PgiResponse {
        try {
            val sekvensnummer = hendelse.sekvensnummer
            val marker = Markers.append("sekvensnummer", sekvensnummer.toString())

            LOG.info(marker, "Leser pgi fra skatt for ${hendelse.toString().maskFnr()}. Sekvensnummer: $sekvensnummer")
            SECURE_LOG.info(marker, "Leser pgi fra skatt for ${hendelse}. Sekvensnummer: $sekvensnummer")

            val request = pgiClient.createPgiRequest(hendelse.gjelderPeriode, hendelse.identifikator)

            val pgiResponse =  PgiResponse(
                pgiClient.getPgi(request, HttpResponse.BodyHandlers.ofString()),
                createPgiMetadata(hendelse),
                hendelse.identifikator
            )

            return pgiResponse
        } catch (e: Exception) {
            throw PensjonsgivendeInntektClientException("Call to pgi failed with exception: ${e.javaClass.simpleName}. Message: ${e.message} ")
        }
    }

    private fun createPgiMetadata(hendelse: Hendelse): PensjonsgivendeInntektMetadata =
        PensjonsgivendeInntektMetadata(hendelse.metaData.retries, hendelse.sekvensnummer)

    companion object {
        private val SECURE_LOG: Logger = LoggerFactory.getLogger("team")
        private val LOG = LoggerFactory.getLogger(FetchPgiFromSkatt::class.java)
    }
}

class PensjonsgivendeInntektClientException(message: String) : RuntimeException(message.maskFnr())