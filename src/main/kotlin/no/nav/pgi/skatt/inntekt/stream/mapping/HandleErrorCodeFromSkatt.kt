package no.nav.pgi.skatt.inntekt.stream.mapping

import no.nav.pensjon.samhandling.maskfnr.maskFnr
import org.apache.kafka.streams.kstream.ValueMapper
import java.net.http.HttpResponse

internal class HandleErrorCodeFromSkatt : ValueMapper<HttpResponse<String>, String> {
    override fun apply(response: HttpResponse<String>): String {
        return when {
            response.statusCode() == 200 -> response.body()
            response.statusCode() == 400 && response hasErrorMessage "PGIF-005" -> throw UnsupportedInntektsAarException("PGIF-005\tDet forespurte inntektsåret er ikke støttet")
            response.statusCode() == 400 && response hasErrorMessage "PGIF-007" -> throw InvalidInntektsAarFormatException("PGIF-007\tInntektsår har ikke gyldig format")
            response.statusCode() == 400 && response hasErrorMessage "PGIF-008" -> throw InvalidPersonidentifikatorFormatException("PGIF-008\tPersonidentifikator har ikke gyldig format")
            response.statusCode() == 404 && response hasErrorMessage "PGIF-006" -> throw PgiForYearAndIdentifierNotFoundException("PGIF-006\tFant ikke PGI for angitt inntektsår og identifikator")
            response.statusCode() == 404 && response hasErrorMessage "PGIF-009" -> throw NoPersonWithGivenIdentifikatorException("PGIF-009\tFant ingen person for gitt identifikator")
            else -> throw UnhandledStatusCodeException("Call to pgi failed with code: ${response.statusCode()} and body: ${response.body()}")
        }
    }
}

infix fun HttpResponse<String>.hasErrorMessage(errorMessage: String) = body().contains(errorMessage)

class UnsupportedInntektsAarException(message: String) : RuntimeException(message.maskFnr())
class PgiForYearAndIdentifierNotFoundException(message: String) : RuntimeException(message.maskFnr())
class InvalidInntektsAarFormatException(message: String) : RuntimeException(message.maskFnr())
class UnhandledStatusCodeException(message: String) : RuntimeException(message.maskFnr())
class InvalidPersonidentifikatorFormatException(message: String) : RuntimeException(message.maskFnr())
class NoPersonWithGivenIdentifikatorException(message: String) : RuntimeException(message.maskFnr())


/*
Når det gjelder feilmeldinger som er spesielle i kallet for å hente ut grunnlaget så har vi til nå:
HTTP kode	Feilkode	Tekst
400	PGIF-005	Det forespurte inntektsåret er ikke støttet
404	PGIF-006	Fant ikke PGI for angitt inntektsår og identifikator
400	PGIF-007	Inntektsår har ikke gyldig format
400	PGIF-008	Personidentifikator har ikke gyldig format
404	PGIF-009	Fant ingen person for gitt identifikator
*/