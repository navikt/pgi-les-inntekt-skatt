package no.nav.pgi.skatt.inntekt.stream.mapping

import no.nav.pensjon.samhandling.maskfnr.maskFnr
import org.apache.kafka.streams.kstream.ValueMapper

internal class HandleErrorCodeFromSkatt : ValueMapper<PgiResponse, PgiResponse> {
    override fun apply(response: PgiResponse): PgiResponse {
        return when {
            response.statusCode() == 200 -> response
            response.statusCode() == 400 && response hasErrorMessage "PGIF-005" -> throw UnsupportedInntektsAarException("PGIF-005\tDet forespurte inntektsåret er ikke støttet. ${response.traceString()}")
            response.statusCode() == 400 && response hasErrorMessage "PGIF-007" -> throw InvalidInntektsAarFormatException("PGIF-007\tInntektsår har ikke gyldig format. ${response.traceString()}")
            response.statusCode() == 400 && response hasErrorMessage "PGIF-008" -> throw InvalidPersonidentifikatorFormatException("PGIF-008\tPersonidentifikator har ikke gyldig format. ${response.traceString()}")
            response.statusCode() == 404 && response hasErrorMessage "PGIF-006" -> throw PgiForYearAndIdentifierNotFoundException("PGIF-006\tFant ikke PGI for angitt inntektsår og identifikator. ${response.traceString()}")
            response.statusCode() == 404 && response hasErrorMessage "PGIF-009" -> throw NoPersonWithGivenIdentifikatorException("PGIF-009\tFant ingen person for gitt identifikator. ${response.traceString()}")
            else -> throw UnhandledStatusCodeException("Call to pgi failed with code: ${response.statusCode()} and body: ${response.body()}. ${response.traceString()}")
        }
    }
}

class UnsupportedInntektsAarException(message: String) : RuntimeException(message.maskFnr())
class PgiForYearAndIdentifierNotFoundException(message: String) : RuntimeException(message.maskFnr())
class InvalidInntektsAarFormatException(message: String) : RuntimeException(message.maskFnr())
class UnhandledStatusCodeException(message: String) : RuntimeException(message.maskFnr())
class InvalidPersonidentifikatorFormatException(message: String) : RuntimeException(message.maskFnr())
class NoPersonWithGivenIdentifikatorException(message: String) : RuntimeException(message.maskFnr())