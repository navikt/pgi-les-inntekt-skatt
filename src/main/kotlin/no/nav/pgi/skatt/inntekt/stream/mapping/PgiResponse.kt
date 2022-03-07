package no.nav.pgi.skatt.inntekt.stream.mapping

import no.nav.samordning.pgi.schema.PensjonsgivendeInntektMetadata
import java.net.http.HttpResponse

data class PgiResponse(val httpResponse: HttpResponse<String>, val metadata: PensjonsgivendeInntektMetadata, private val identifikator: String = "***********") {
    internal fun statusCode() = httpResponse.statusCode()
    internal fun body() = httpResponse.body()
    internal fun metadata() = metadata
    internal fun traceString() = createTraceableSekvensnummerString(metadata.getSekvensnummer())
    internal fun identifikator() = identifikator
}

internal fun createTraceableSekvensnummerString(sekvensnummer: Long?) = """ ("sekvensnummer": $sekvensnummer)"""