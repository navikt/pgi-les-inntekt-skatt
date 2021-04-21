package no.nav.pgi.skatt.inntekt.stream.mapping

import io.prometheus.client.Counter
import no.nav.pensjon.samhandling.maskfnr.maskFnr
import org.apache.kafka.streams.kstream.ValueMapper

private val pgiLesInntektSkattResponseCounter = Counter.build()
    .name("pgi_les_inntekt_skatt_response_counter")
    .labelNames("statusCode")
    .help("Count response status codes from popp")
    .register()

private val handledErrorCodes =
    listOf("PGIF-005", "PGIF-006", "PGIF-007", "PGIF-008", "PGIF-009", "DAS-001", "DAS-002", "DAS-003", "DAS-004", "DAS-005", "DAS-006", "DAS-007", "DAS-008")

internal class HandleErrorCodeFromSkatt : ValueMapper<PgiResponse, PgiResponse> {
    override fun apply(response: PgiResponse): PgiResponse {
        return when {
            response.statusCode() == 200 -> {
                pgiLesInntektSkattResponseCounter.labels("${response.statusCode()}").inc()
                response
            }
            else -> {
                pgiLesInntektSkattResponseCounter.labels(createErrorLabel(response)).inc()
                throw FeilmedlingFraSkattException("Call to pgi failed with code: ${response.statusCode()} and body: ${response.body()}. ${response.traceString()}")
            }
        }
    }

    private fun createErrorLabel(response: PgiResponse) = "${response.statusCode()}${response.getErrorMessage(handledErrorCodes)?.let { "_$it" } ?: ""}"
}

class FeilmedlingFraSkattException(message: String) : RuntimeException(message.maskFnr())