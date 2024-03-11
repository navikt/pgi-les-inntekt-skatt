package no.nav.pgi.skatt.inntekt.stream.mapping

import io.prometheus.client.Counter
import no.nav.pensjon.samhandling.maskfnr.maskFnr
import no.nav.pgi.skatt.inntekt.skatt.PgiFolketrygdenErrorCodes.Companion.pgiFolketrygdenErrorCodes
import no.nav.pgi.skatt.inntekt.skatt.getFirstMatch
import org.apache.kafka.streams.kstream.ValueMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val pgiLesInntektSkattResponseCounter = Counter.build()
    .name("pgi_les_inntekt_skatt_response_counter")
    .labelNames("statusCode")
    .help("Count response status codes from popp")
    .register()

internal class HandleErrorCodeFromSkatt : ValueMapper<PgiResponse, PgiResponse> {
    override fun apply(response: PgiResponse): PgiResponse? {
        return when {
            response.statusCode() == 200 -> {
                pgiLesInntektSkattResponseCounter.labels("${response.statusCode()}").inc()
                response
            }

            else -> {
                pgiLesInntektSkattResponseCounter.labels(createErrorLabel(response)).inc()
                SECURE_LOG.error("Call to pgi failed with code: ${response.statusCode()}, body: ${response.body()} and fnr ${response.identifikator()}. ${response.traceString()}")
                throw FeilmedlingFraSkattException("Call to pgi failed with code: ${response.statusCode()} and body: ${response.body()}. ${response.traceString()}")
            }
        }
    }

    private fun createErrorLabel(response: PgiResponse) =
        "${response.statusCode()}${(response.body() getFirstMatch pgiFolketrygdenErrorCodes)?.let { "_$it" } ?: ""}"

    companion object {
        private val SECURE_LOG: Logger = LoggerFactory.getLogger("tjenestekall")
        private val LOG = LoggerFactory.getLogger(HandleErrorCodeFromSkatt::class.java)
    }
}

class FeilmedlingFraSkattException(message: String) : RuntimeException(message.maskFnr())