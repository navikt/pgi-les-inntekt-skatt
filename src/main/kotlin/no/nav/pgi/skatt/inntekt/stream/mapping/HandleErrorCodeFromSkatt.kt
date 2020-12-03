package no.nav.pgi.skatt.inntekt.stream.mapping

import no.nav.pensjon.samhandling.maskfnr.maskFnr
import org.apache.kafka.streams.kstream.ValueMapper
import java.net.http.HttpResponse

internal class HandleErrorCodeFromSkatt : ValueMapper<HttpResponse<String>, String> {
    override fun apply(httpResponse: HttpResponse<String>): String {
        return when {
            httpResponse.statusCode() == 200 -> httpResponse.body()
            else -> handleError(httpResponse)
        }
    }

    private fun handleError(httpResponse: HttpResponse<String>): String {
        throw UnhandledStatusCodeException("Call to pgi failed with code: ${httpResponse.statusCode()} and body: ${httpResponse.body()}")
    }
}

class UnhandledStatusCodeException(message: String) : RuntimeException(message.maskFnr())