package no.nav.pgi.skatt.inntekt.stream.mapping

import io.mockk.every
import io.mockk.mockk
import org.apache.kafka.streams.kstream.ValueMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.net.http.HttpResponse

private const val DUMMY_BODY = "test body"

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Suppress("UNCHECKED_CAST")
internal class HandleErrorCodeFromSkattTest {
    private val handleErrorCodesMapper: ValueMapper<HttpResponse<String>, String> = HandleErrorCodeFromSkatt()


    @Test
    internal fun `should return body when status 200`() {
        assertEquals(DUMMY_BODY, handleErrorCodesMapper.apply(mockkHttpResponse(DUMMY_BODY, 200)))
    }

    @Test
    internal fun `should throw UnhandledStatusCodeException when status is not handled`() {
        assertThrows<UnhandledStatusCodeException> { handleErrorCodesMapper.apply(mockkHttpResponse(DUMMY_BODY, 404)) }
    }
    @Test
    internal fun `should throw UnsupportedInntektsAarException when status is not handled`() {
        assertThrows<UnsupportedInntektsAarException> {
            handleErrorCodesMapper.apply(mockkHttpResponse("PGIF-005", 400))
        }
    }
    @Test
    internal fun `should throw PgiForYearAndIdentifierNotFoundException when status is not handled`() {
        assertThrows<PgiForYearAndIdentifierNotFoundException> {
            handleErrorCodesMapper.apply(mockkHttpResponse("PGIF-006", 404))
        }
    }

    @Test
    internal fun `should throw InvalidInntektsAarFormatException when status is not handled`() {
         assertThrows<InvalidInntektsAarFormatException> {
             handleErrorCodesMapper.apply(mockkHttpResponse("PGIF-007", 400))
         }
    }

    @Test
    internal fun `should throw InvalidPersonidentifikatorFormatException when status is not handled`() {
        assertThrows<InvalidPersonidentifikatorFormatException> {
            handleErrorCodesMapper.apply(mockkHttpResponse("PGIF-008", 400))
        }
    }

    @Test
    internal fun `should throw NoPersonWithGivenIdentifikatorException when status is not handled`() {
        assertThrows<NoPersonWithGivenIdentifikatorException> {
            handleErrorCodesMapper.apply(mockkHttpResponse("PGIF-009", 404))
        }
    }

    fun mockkHttpResponse(body: String, statusCode: Int) =
        mockk<HttpResponse<String>>().apply {
            every { body() } returns body
            every { statusCode() } returns (statusCode)
        }
}

/*
Når det gjelder feilmeldinger som er spesielle i kallet for å hente ut grunnlaget så har vi til nå:
HTTP kode	Feilkode	Tekst
400	PGIF-005	Det forespurte inntektsåret er ikke støttet
404	PGIF-006	Fant ikke PGI for angitt inntektsår og identifikator
400	PGIF-007	Inntektsår har ikke gyldig format
400	PGIF-008	Personidentifikator har ikke gyldig format
404	PGIF-009	Fant ingen person for gitt identifikator
*/