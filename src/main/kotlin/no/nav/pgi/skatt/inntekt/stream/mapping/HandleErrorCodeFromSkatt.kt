package no.nav.pgi.skatt.inntekt.stream.mapping

import io.prometheus.client.Counter
import no.nav.pensjon.samhandling.maskfnr.maskFnr
import no.nav.pgi.skatt.inntekt.skatt.ErrorCodesSkatt.Companion.skattAllErrorCodes
import no.nav.pgi.skatt.inntekt.skatt.ErrorCodesSkatt.Companion.skattDiscardErrorCodes
import no.nav.pgi.skatt.inntekt.skatt.containOneOf
import no.nav.pgi.skatt.inntekt.skatt.getFirstMatch
import org.apache.kafka.streams.kstream.ValueMapper
import org.slf4j.LoggerFactory

private val pgiLesInntektSkattResponseCounter = Counter.build()
    .name("pgi_les_inntekt_skatt_response_counter")
    .labelNames("statusCode")
    .help("Count response status codes from popp")
    .register()

private val LOG = LoggerFactory.getLogger(HandleErrorCodeFromSkatt::class.java)

internal class HandleErrorCodeFromSkatt : ValueMapper<PgiResponse, PgiResponse> {
    override fun apply(response: PgiResponse): PgiResponse? {
        return when {
            response.statusCode() == 200 -> {
                pgiLesInntektSkattResponseCounter.labels("${response.statusCode()}").inc()
                response
            }
            response.body() containOneOf skattDiscardErrorCodes -> {
                pgiLesInntektSkattResponseCounter.labels(createErrorLabel(response)).inc()
                LOG.error("Feil på api Pgi Folketrygd, kontakt skatt! Call to pgi failed with code: ${response.statusCode()} and body: ${response.body()}. ${response.traceString()}")
                null
            }
            else -> {
                pgiLesInntektSkattResponseCounter.labels(createErrorLabel(response)).inc()
                throw FeilmedlingFraSkattException("Call to pgi failed with code: ${response.statusCode()} and body: ${response.body()}. ${response.traceString()}")
            }
        }
    }

    private fun createErrorLabel(response: PgiResponse) = "${response.statusCode()}${(response.body() getFirstMatch skattAllErrorCodes)?.let { "_$it" } ?: ""}"
}

class FeilmedlingFraSkattException(message: String) : RuntimeException(message.maskFnr())