package no.nav.pgi.skatt.inntekt.stream.mapping

import io.mockk.every
import io.mockk.mockk
import no.nav.samordning.pgi.schema.PensjonsgivendeInntektMetadata
import org.apache.kafka.streams.kstream.ValueMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.http.HttpResponse

private const val DUMMY_BODY = "test body"

internal class HandleErrorCodeFromSkattTest {
    private val handleErrorCodesMapper: ValueMapper<PgiResponse, PgiResponse> =
        HandleErrorCodeFromSkatt()


    @Test
    internal fun `should return body when status 200`() {
        assertEquals(DUMMY_BODY, handleErrorCodesMapper.apply(mockkHttpResponse(DUMMY_BODY, 200)).body())
    }

    @Test
    internal fun `should throw FeilmedlingFraSkattException when status is not handled`() {
        assertThrows<FeilmedlingFraSkattException> { handleErrorCodesMapper.apply(mockkHttpResponse(DUMMY_BODY, 404)) }
    }

    @Test
    internal fun `should throw FeilmedlingFraSkattException when error message contains PGIF-005`() {
        assertNull(handleErrorCodesMapper.apply(mockkHttpResponse("PGIF-005", 400)))
    }

    @Test
    internal fun `should throw FeilmedlingFraSkattException when error message contains PGIF-006`() {
        assertNull(handleErrorCodesMapper.apply(mockkHttpResponse("PGIF-006", 404)))
    }

    @Test
    internal fun `should throw FeilmedlingFraSkattException when error message contains PGIF-007`() {
        assertNull(handleErrorCodesMapper.apply(mockkHttpResponse("PGIF-007", 400)))
    }

    @Test
    internal fun `should return null when error message contains PGIF-008`() {
        assertNull(handleErrorCodesMapper.apply(mockkHttpResponse("PGIF-008", 400)))
    }


    @Test
    internal fun `should throw FeilmedlingFraSkattException when error message contains PGIF-009`() {
        assertNull(handleErrorCodesMapper.apply(mockkHttpResponse("PGIF-009", 404)))
    }

    @Test
    internal fun `should throw FeilmedlingFraSkattException when 500 error contains message DAS-001`() {
        assertThrows<FeilmedlingFraSkattException> {
            handleErrorCodesMapper.apply(
                mockkHttpResponse(
                    """{"kode":"DAS-001","melding":"Fant ingen person for gitt identifikator","korrelasjonsid":"e81bef15-b211-43d7-9c03-8d1d33205a5a"}""",
                    500
                )
            )
        }
    }

    @Test
    internal fun `should throw FeilmedlingFraSkattException when 404 error contains message DAS-002`() {
        assertThrows<FeilmedlingFraSkattException> {
            handleErrorCodesMapper.apply(mockkHttpResponse("DAS-002", 404))
        }
    }

    @Test
    internal fun `should throw FeilmedlingFraSkattException when 500 error contains message DAS-003`() {
        assertThrows<FeilmedlingFraSkattException> {
            handleErrorCodesMapper.apply(mockkHttpResponse("DAS-004", 500))
        }
    }

    @Test
    internal fun `should throw FeilmedlingFraSkattException when 500 error contains message DAS-004`() {
        assertThrows<FeilmedlingFraSkattException> {
            handleErrorCodesMapper.apply(mockkHttpResponse("DAS-004", 500))
        }
    }

    @Test
    internal fun `should throw FeilmedlingFraSkattException when 500 error contains message DAS-005`() {
        assertThrows<FeilmedlingFraSkattException> {
            handleErrorCodesMapper.apply(mockkHttpResponse("DAS-005", 500))
        }
    }

    @Test
    internal fun `should throw FeilmedlingFraSkattException when 500 error contains message DAS-006`() {
        assertThrows<FeilmedlingFraSkattException> {
            handleErrorCodesMapper.apply(mockkHttpResponse("DAS-006", 500))
        }
    }

    @Test
    internal fun `should throw FeilmedlingFraSkattException when 500 error contains message DAS-007`() {
        assertThrows<FeilmedlingFraSkattException> {
            handleErrorCodesMapper.apply(mockkHttpResponse("DAS-007", 500))
        }
    }

    @Test
    internal fun `should throw FeilmedlingFraSkattException when 403 error contains message DAS-008`() {
        assertThrows<FeilmedlingFraSkattException> {
            handleErrorCodesMapper.apply(mockkHttpResponse("DAS-008", 403))
        }
    }

    @Test
    internal fun `should throw FeilmedlingFraSkattException when unknown DAS-error`() {
        assertThrows<FeilmedlingFraSkattException> {
            handleErrorCodesMapper.apply(mockkHttpResponse("DAS-009", 500))
        }
    }

    private fun mockkHttpResponse(body: String, statusCode: Int): PgiResponse {
        val mockHttpResponse = mockk<HttpResponse<String>>()
        every { mockHttpResponse.hint(String::class).body() } returns body
        every { mockHttpResponse.statusCode() } returns (statusCode)
        return PgiResponse(mockHttpResponse, PensjonsgivendeInntektMetadata())
    }
}