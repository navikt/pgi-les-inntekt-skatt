package no.nav.pgi.skatt.inntekt.stream.mapping

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
    private val mapper: ValueMapper<HttpResponse<String>, String> = HandleErrorCodeFromSkatt()

    @Test
    internal fun `should return body when status 200`() {
        val httpResponse = createMockHttpResponse()
        Mockito.`when`(httpResponse.body()).thenReturn(DUMMY_BODY)
        Mockito.`when`(httpResponse.statusCode()).thenReturn(200)

        assertEquals(DUMMY_BODY, mapper.apply(httpResponse))
    }

    @Test
    internal fun `should throw UnhandledStatusCodeException when status is not handled`() {
        val httpResponse = createMockHttpResponse()
        Mockito.`when`(httpResponse.body()).thenReturn(DUMMY_BODY)
        Mockito.`when`(httpResponse.statusCode()).thenReturn(404)

        assertThrows<UnhandledStatusCodeException> { mapper.apply(httpResponse) }
    }

    private fun createMockHttpResponse(): HttpResponse<String> = Mockito.mock(HttpResponse::class.java) as HttpResponse<String>
}
