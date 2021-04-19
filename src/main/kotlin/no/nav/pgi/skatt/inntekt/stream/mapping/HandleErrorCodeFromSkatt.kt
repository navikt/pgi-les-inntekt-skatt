package no.nav.pgi.skatt.inntekt.stream.mapping

import io.prometheus.client.Counter
import no.nav.pensjon.samhandling.maskfnr.maskFnr
import org.apache.kafka.streams.kstream.ValueMapper

private val pgiLesInntektSkattResponseCounter = Counter.build()
    .name("pgi_les_inntekt_skatt_response_counter")
    .labelNames("statusCode")
    .help("Count response status codes from popp")
    .register()

internal class HandleErrorCodeFromSkatt : ValueMapper<PgiResponse, PgiResponse> {
    override fun apply(response: PgiResponse): PgiResponse {

        return when {
            response.statusCode() == 200 -> returnResponse(response)
            response.statusCode() == 400 && response hasErrorMessage "PGIF-005" -> throwUnsupportedInntektsAarException(response)
            response.statusCode() == 400 && response hasErrorMessage "PGIF-007" -> throwInvalidInntektsAarFormatException(response)
            response.statusCode() == 400 && response hasErrorMessage "PGIF-008" -> throwInvalidPersonidentifikatorFormatException(response)
            response.statusCode() == 404 && response hasErrorMessage "PGIF-006" -> throwPgiForYearAndIdentifierNotFoundException(response)
            response.statusCode() == 404 && response hasErrorMessage "PGIF-009" -> throwNoPersonWithGivenIdentifikatorException(response)
            else -> throwUnhandledStatusCodeException(response)
        }
    }

    private fun returnResponse(response: PgiResponse): PgiResponse {
      pgiLesInntektSkattResponseCounter.labels("${response.statusCode()}_OK").inc()
      return response
    }

    private fun throwUnsupportedInntektsAarException(response: PgiResponse) : PgiResponse {
        pgiLesInntektSkattResponseCounter.labels("${response.statusCode()}_PGIF-005:Unsupported_Inntekt_Aar").inc()
        throw UnsupportedInntektsAarException("PGIF-005\tDet forespurte inntektsåret er ikke støttet. ${response.traceString()}")
    }

    private fun throwInvalidInntektsAarFormatException(response: PgiResponse) : PgiResponse {
        pgiLesInntektSkattResponseCounter.labels("${response.statusCode()}_PGIF-007:InvalidInntektAarFormat").inc()
        throw InvalidInntektsAarFormatException("PGIF-007\tInntektsår har ikke gyldig format. ${response.traceString()}")
    }

    private fun throwInvalidPersonidentifikatorFormatException(response: PgiResponse) : PgiResponse {
        pgiLesInntektSkattResponseCounter.labels("${response.statusCode()}_PGIF-008:InvalidPersonidentifikatorFormat").inc()
        throw InvalidPersonidentifikatorFormatException("PGIF-008\tPersonidentifikator har ikke gyldig format. ${response.traceString()}")
    }

    private fun throwPgiForYearAndIdentifierNotFoundException(response: PgiResponse) : PgiResponse {
        pgiLesInntektSkattResponseCounter.labels("${response.statusCode()}_PGIF-006:PgiForYearAndIdentifierNotFound").inc()
        throw PgiForYearAndIdentifierNotFoundException("PGIF-006\tFant ikke PGI for angitt inntektsår og identifikator. ${response.traceString()}")
    }

    private fun throwNoPersonWithGivenIdentifikatorException(response: PgiResponse) : PgiResponse {
        pgiLesInntektSkattResponseCounter.labels("${response.statusCode()}_PGIF-009:NoPersonWithGivenIdentifikator").inc()
        throw NoPersonWithGivenIdentifikatorException("PGIF-009\tFant ingen person for gitt identifikator. ${response.traceString()}")
    }

    private fun throwUnhandledStatusCodeException(response: PgiResponse) : PgiResponse {
        pgiLesInntektSkattResponseCounter.labels("${response.statusCode()}_UnhandeledStatusCode").inc()
        throw UnhandledStatusCodeException("Call to pgi failed with code: ${response.statusCode()} and body: ${response.body()}. ${response.traceString()}")
    }

}

class UnsupportedInntektsAarException(message: String) : RuntimeException(message.maskFnr())
class PgiForYearAndIdentifierNotFoundException(message: String) : RuntimeException(message.maskFnr())
class InvalidInntektsAarFormatException(message: String) : RuntimeException(message.maskFnr())
class UnhandledStatusCodeException(message: String) : RuntimeException(message.maskFnr())
class InvalidPersonidentifikatorFormatException(message: String) : RuntimeException(message.maskFnr())
class NoPersonWithGivenIdentifikatorException(message: String) : RuntimeException(message.maskFnr())