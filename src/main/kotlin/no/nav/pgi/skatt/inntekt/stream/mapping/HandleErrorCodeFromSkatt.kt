package no.nav.pgi.skatt.inntekt.stream.mapping

import net.logstash.logback.marker.Markers
import no.nav.pgi.skatt.inntekt.Counters
import no.nav.pgi.skatt.inntekt.util.maskFnr
import no.nav.pgi.skatt.inntekt.skatt.PgiFolketrygdenErrorCodes.Companion.pgiFolketrygdenErrorCodes
import no.nav.pgi.skatt.inntekt.skatt.getFirstMatch
import org.apache.kafka.streams.kstream.ValueMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory


internal class HandleErrorCodeFromSkatt(val counters: Counters) : ValueMapper<PgiResponse, PgiResponse> {
    override fun apply(response: PgiResponse): PgiResponse {
        return when {
            response.statusCode() == 200 -> {
                counters.increasePgiLesInntektSkattResponseCounter("${response.statusCode()}")
                response
            }

            else -> {

                counters.increasePgiLesInntektSkattResponseCounter(createErrorLabel(response))
                val marker = Markers.append("sekvensnummer", response.sekvensnummer().toString())
                LOG.error(marker, "Call to pgi failed with code: ${response.statusCode()}, body: ${response.body()} and fnr ${response.identifikator().maskFnr()} ")
                SECURE_LOG.error(marker, "Call to pgi failed with code: ${response.statusCode()}, body: ${response.body()} and fnr ${response.identifikator()} ")
                throw FeilmedlingFraSkattException("Call to pgi failed with code: ${response.statusCode()} and body: ${response.body()} ")
            }
        }
    }

    private fun createErrorLabel(response: PgiResponse) =
        "${response.statusCode()}${(response.body() getFirstMatch pgiFolketrygdenErrorCodes)?.let { "_$it" } ?: ""}"

    companion object {
        private val SECURE_LOG: Logger = LoggerFactory.getLogger("team")
        private val LOG = LoggerFactory.getLogger(HandleErrorCodeFromSkatt::class.java)
    }
}

class FeilmedlingFraSkattException(message: String) : RuntimeException(message.maskFnr())