package no.nav.pgi.skatt.inntekt.stream.mapping

import io.mockk.every
import io.mockk.mockk
import org.apache.kafka.streams.kstream.ValueMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.net.http.HttpResponse

private const val DUMMY_BODY = "test body"

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Suppress("UNCHECKED_CAST")
internal class HandleErrorCodeFromSkattTest {
    private val handleErrorCodesMapper: ValueMapper<HttpResponse<String>, String> = HandleErrorCodeFromSkatt()

    @Test
    internal fun `should return body when status 200`() {
        val httpResponse = mockk<HttpResponse<String>>() 
        every { httpResponse.body() } returns DUMMY_BODY
        every { httpResponse.statusCode() } returns (200)

        assertEquals(DUMMY_BODY, handleErrorCodesMapper.apply(httpResponse))
    }

    @Test
    internal fun `should throw UnhandledStatusCodeException when status is not handled`() {
        val httpResponse = createMockHttpResponse()
        Mockito.`when`(httpResponse.body()).thenReturn(DUMMY_BODY)
        Mockito.`when`(httpResponse.statusCode()).thenReturn(404)

        assertThrows<UnhandledStatusCodeException> { handleErrorCodesMapper.apply(httpResponse) }
    }


    @Test
    internal fun `should throw UnsupportedInntektsAarException when status is not handled`() {
        val httpResponse = createMockHttpResponse()
        Mockito.`when`(httpResponse.body()).thenReturn("PGIF-005")
        Mockito.`when`(httpResponse.statusCode()).thenReturn(400)

        assertThrows<UnsupportedInntektsAarException> { handleErrorCodesMapper.apply(httpResponse) }
    }

    @Test
    internal fun `should throw PgiForYearAndIdentifierNotFoundException when status is not handled`() {
        val httpResponse = createMockHttpResponse()
        Mockito.`when`(httpResponse.body()).thenReturn("PGIF-006")
        Mockito.`when`(httpResponse.statusCode()).thenReturn(404)

        assertThrows<PgiForYearAndIdentifierNotFoundException> { handleErrorCodesMapper.apply(httpResponse) }
    }

    @Test
    internal fun `should throw InvalidInntektsAarFormatException when status is not handled`() {
        val httpResponse = createMockHttpResponse()
        Mockito.`when`(httpResponse.body()).thenReturn("PGIF-007")
        Mockito.`when`(httpResponse.statusCode()).thenReturn(400)

        assertThrows<InvalidInntektsAarFormatException> { handleErrorCodesMapper.apply(httpResponse) }
    }

    @Test
    internal fun `should throw InvalidPersonidentifikatorFormatException when status is not handled`() {
        val httpResponse = createMockHttpResponse()
        Mockito.`when`(httpResponse.body()).thenReturn("PGIF-008")
        Mockito.`when`(httpResponse.statusCode()).thenReturn(400)

        assertThrows<InvalidPersonidentifikatorFormatException> { handleErrorCodesMapper.apply(httpResponse) }
    }

    @Test
    internal fun `should throw NoPersonWithGivenIdentifikatorException when status is not handled`() {
        val httpResponse = createMockHttpResponse()
        Mockito.`when`(httpResponse.body()).thenReturn("PGIF-009")
        Mockito.`when`(httpResponse.statusCode()).thenReturn(404)

        assertThrows<NoPersonWithGivenIdentifikatorException> { handleErrorCodesMapper.apply(httpResponse) }
    }

    private fun createMockHttpResponse(): HttpResponse<String> = Mockito.mock(HttpResponse::class.java) as HttpResponse<String>
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