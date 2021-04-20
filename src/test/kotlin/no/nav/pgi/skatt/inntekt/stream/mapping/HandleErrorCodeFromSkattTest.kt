package no.nav.pgi.skatt.inntekt.stream.mapping

import io.mockk.every
import io.mockk.mockk
import no.nav.samordning.pgi.schema.PensjonsgivendeInntektMetadata
import org.apache.kafka.streams.kstream.ValueMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.net.http.HttpResponse

private const val DUMMY_BODY = "test body"

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class HandleErrorCodeFromSkattTest {
    private val handleErrorCodesMapper: ValueMapper<PgiResponse, PgiResponse> =
        HandleErrorCodeFromSkatt()

    @Test
    internal fun `should return body when status 200`() {
        assertEquals(DUMMY_BODY, handleErrorCodesMapper.apply(mockkHttpResponse(DUMMY_BODY, 200)).body())
    }

    @Test
    internal fun `should throw UnhandledStatusCodeException when status is not handled`() {
        assertThrows<UnhandledStatusCodeException> { handleErrorCodesMapper.apply(mockkHttpResponse(DUMMY_BODY, 404)) }
    }

    @Test
    internal fun `should throw UnsupportedInntektsAarException when error message contains PGIF-005`() {
        assertThrows<UnsupportedInntektsAarException> {
            handleErrorCodesMapper.apply(mockkHttpResponse("PGIF-005", 400))
        }
    }

    @Test
    internal fun `should throw PgiForYearAndIdentifierNotFoundException when error message contains PGIF-006`() {
        assertThrows<PgiForYearAndIdentifierNotFoundException> {
            handleErrorCodesMapper.apply(mockkHttpResponse("PGIF-006", 404))
        }
    }

    @Test
    internal fun `should throw InvalidInntektsAarFormatException when error message contains PGIF-007`() {
        assertThrows<InvalidInntektsAarFormatException> {
            handleErrorCodesMapper.apply(mockkHttpResponse("PGIF-007", 400))
        }
    }

    @Test
    internal fun `should throw InvalidPersonidentifikatorFormatException when error message contains PGIF-008`() {
        assertThrows<InvalidPersonidentifikatorFormatException> {
            handleErrorCodesMapper.apply(mockkHttpResponse("PGIF-008", 400))
        }
    }

    @Test
    internal fun `should throw NoPersonWithGivenIdentifikatorException when error message contains PGIF-009`() {
        assertThrows<NoPersonWithGivenIdentifikatorException> {
            handleErrorCodesMapper.apply(mockkHttpResponse("PGIF-009", 404))
        }
    }

    @Test
    internal fun `should throw SkattCommonError when 500 error contains message DAS-001`() {
        assertThrows<SkattCommonError> {
            handleErrorCodesMapper.apply(mockkHttpResponse("DAS-001", 500))
        }
    }

    @Test
    internal fun `should throw SkattCommonError when 404 error contains message DAS-002`() {
        assertThrows<SkattCommonError> {
            handleErrorCodesMapper.apply(mockkHttpResponse("DAS-002", 404))
        }
    }

    @Test
    internal fun `should throw SkattCommonError when 500 error contains message DAS-003`() {
        assertThrows<SkattCommonError> {
            handleErrorCodesMapper.apply(mockkHttpResponse("DAS-004", 500))
        }
    }

    @Test
    internal fun `should throw SkattCommonError when 500 error contains message DAS-004`() {
        assertThrows<SkattCommonError> {
            handleErrorCodesMapper.apply(mockkHttpResponse("DAS-004", 500))
        }
    }

    @Test
    internal fun `should throw SkattCommonError when 500 error contains message DAS-005`() {
        assertThrows<SkattCommonError> {
            handleErrorCodesMapper.apply(mockkHttpResponse("DAS-005", 500))
        }
    }

    @Test
    internal fun `should throw SkattCommonError when 500 error contains message DAS-006`() {
        assertThrows<SkattCommonError> {
            handleErrorCodesMapper.apply(mockkHttpResponse("DAS-006", 500))
        }
    }

    @Test
    internal fun `should throw SkattCommonError when 500 error contains message DAS-007`() {
        assertThrows<SkattCommonError> {
            handleErrorCodesMapper.apply(mockkHttpResponse("DAS-007", 500))
        }
    }

    @Test
    internal fun `should throw SkattCommonError when 403 error contains message DAS-008`() {
        assertThrows<SkattCommonError> {
            handleErrorCodesMapper.apply(mockkHttpResponse("DAS-008", 403))
        }
    }

    @Test
    internal fun `should throw UnhandledStatusCodeException when unknown DAS-error`() {
        assertThrows<UnhandledStatusCodeException> {
            handleErrorCodesMapper.apply(mockkHttpResponse("PGIF-009", 500))
        }
    }

    private fun mockkHttpResponse(body: String, statusCode: Int): PgiResponse {
        val mockHttpResponse = mockk<HttpResponse<String>>()
        every { mockHttpResponse.hint(String::class).body() } returns body
        every { mockHttpResponse.statusCode() } returns (statusCode)
        return PgiResponse(mockHttpResponse, PensjonsgivendeInntektMetadata())
    }
}