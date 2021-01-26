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

    private fun handleError(httpResponse: HttpResponse<String>): Nothing = when {
        httpResponse.statusCode() == 400 && httpResponse.body().contains("PGIF-005") -> throw UnsupportedInntektsAarException("LOL")
        httpResponse.statusCode() == 400 && httpResponse.body().contains("PGIF-007") -> throw InvalidInntektsAarFormatException("LOL")
        httpResponse.statusCode() == 400 && httpResponse.body().contains("PGIF-008") -> throw InvalidPersonidentifikatorFormatException("LOL")
        httpResponse.statusCode() == 404 && httpResponse.body().contains("PGIF-006") -> throw PgiForYearAndIdentifierNotFoundException("LOL")
        httpResponse.statusCode() == 404 && httpResponse.body().contains("PGIF-009") -> throw NoPersonWithGivenIdentifikatorException("LOL")
        else -> throw UnhandledStatusCodeException("Call to pgi failed with code: ${httpResponse.statusCode()} and body: ${httpResponse.body()}")
    }
}

class UnsupportedInntektsAarException(message: String) : RuntimeException(message.maskFnr())
class PgiForYearAndIdentifierNotFoundException(message: String) : RuntimeException(message.maskFnr())
class InvalidInntektsAarFormatException(message: String) : RuntimeException(message.maskFnr())
class UnhandledStatusCodeException(message: String) : RuntimeException(message.maskFnr())

class InvalidPersonidentifikatorFormatException(message: String) : RuntimeException(message.maskFnr())
class NoPersonWithGivenIdentifikatorException(message: String) : RuntimeException(message.maskFnr())
