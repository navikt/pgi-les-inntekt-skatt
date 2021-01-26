package no.nav.pgi.skatt.inntekt.stream.mapping

import io.netty.handler.codec.http.HttpStatusClass
import no.nav.pensjon.samhandling.maskfnr.maskFnr
import org.apache.kafka.streams.kstream.ValueMapper
import java.net.URLConnection
import java.net.http.HttpResponse

internal class HandleErrorCodeFromSkatt : ValueMapper<HttpResponse<String>, String> {
    override fun apply(httpResponse: HttpResponse<String>): String {
        return when {
            httpResponse.statusCode() == 200 -> httpResponse.body()
            else -> handleError(httpResponse)
        }
    }

    private fun handleError(response: HttpResponse<String>): Nothing = when {
        response.statusCode() == 400 && response hasErrorMessage "PGIF-005" -> throw UnsupportedInntektsAarException("PGIF-005\tDet forespurte inntektsåret er ikke støttet")
        response.statusCode() == 400 && response hasErrorMessage "PGIF-007" -> throw InvalidInntektsAarFormatException("LOL")
        response.statusCode() == 400 && response hasErrorMessage "PGIF-008" -> throw InvalidPersonidentifikatorFormatException("LOL")
        response.statusCode() == 404 && response hasErrorMessage "PGIF-006" -> throw PgiForYearAndIdentifierNotFoundException("LOL")
        response.statusCode() == 404 && response hasErrorMessage "PGIF-009" -> throw NoPersonWithGivenIdentifikatorException("LOL")
        else -> throw UnhandledStatusCodeException("Call to pgi failed with code: ${response.statusCode()} and body: ${response.body()}")
    }
}

infix fun HttpResponse<String>.hasErrorMessage(errorMessage: String) = body().contains(errorMessage)

class UnsupportedInntektsAarException(message: String) : RuntimeException(message.maskFnr())
class PgiForYearAndIdentifierNotFoundException(message: String) : RuntimeException(message.maskFnr())
class InvalidInntektsAarFormatException(message: String) : RuntimeException(message.maskFnr())
class UnhandledStatusCodeException(message: String) : RuntimeException(message.maskFnr())

class InvalidPersonidentifikatorFormatException(message: String) : RuntimeException(message.maskFnr())
class NoPersonWithGivenIdentifikatorException(message: String) : RuntimeException(message.maskFnr())
