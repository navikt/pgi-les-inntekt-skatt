package no.nav.pgi.skatt.inntekt.stream.mapping

import no.nav.samordning.pgi.schema.PensjonsgivendeInntektMetadata
import java.net.http.HttpResponse

data class PgiResponse(val httpResponse: HttpResponse<String>, val metadata: PensjonsgivendeInntektMetadata) {
    internal fun statusCode() = httpResponse.statusCode()
    internal fun body() = httpResponse.body()
    internal fun metadata() = metadata

    internal infix fun hasErrorMessage(errorMessage: String) = body().contains(errorMessage)
    internal fun traceString() = createTraceableSekvensnummerString(metadata.getSekvensnummer())
}

internal fun createTraceableSekvensnummerString(sekvensnummer: Long?) = """ ("sekvensnummer": $sekvensnummer)"""