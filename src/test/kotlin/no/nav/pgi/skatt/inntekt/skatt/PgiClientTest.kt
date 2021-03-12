package no.nav.pgi.skatt.inntekt.skatt

import com.nimbusds.jwt.SignedJWT
import no.nav.pgi.skatt.inntekt.mock.MaskinportenMock
import no.nav.pgi.skatt.inntekt.mock.MaskinportenMock.Companion.MASKINPORTEN_CLIENT_ENV_VARIABLES
import no.nav.pgi.skatt.inntekt.mock.PensjonsgivendeInntektMock
import no.nav.pgi.skatt.inntekt.mock.PensjonsgivendeInntektMock.Companion.PGI_CLIENT_ENV_VARIABLES
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.net.http.HttpResponse

private const val INNTEKTSAAR = "2018"
private const val NORSK_PERSONIDENTIFIKATOR = "12345678901"

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PgiClientTest {
    private val pgiClient: PgiClient = PgiClient(PGI_CLIENT_ENV_VARIABLES + MASKINPORTEN_CLIENT_ENV_VARIABLES)

    private val pensjonsgivendeInntektMock = PensjonsgivendeInntektMock()
    private val maskinportenMock = MaskinportenMock()

    @BeforeAll
    internal fun init() {
        maskinportenMock.`stub maskinporten token endpoint`()
    }

    @BeforeEach
    internal fun beforeEach() {
        pensjonsgivendeInntektMock.reset()
    }

    @AfterAll
    internal fun teardown() {
        pensjonsgivendeInntektMock.stop()
        maskinportenMock.stop()
    }

    @Test
    fun `createGetRequest should add inntektsaar and norskPersonidentifikator to path`() {
        val request = pgiClient.createPgiRequest(INNTEKTSAAR, NORSK_PERSONIDENTIFIKATOR)
        assertEquals("""$PENSJONSGIVENDE_INNTEKT_PATH/$INNTEKTSAAR/$NORSK_PERSONIDENTIFIKATOR""", request.uri().path)
    }

    @Test
    fun `createGetRequest should add authorization header with bearer token`() {
        val request = pgiClient.createPgiRequest(INNTEKTSAAR, NORSK_PERSONIDENTIFIKATOR)
        val authorizationHeader = request.headers().firstValue("Authorization").get()

        assertTrue(authorizationHeader containsRegex """Bearer\s.*""")
        assertDoesNotThrow { parseJwt(authorizationHeader) }
    }

    @Test
    fun `should return response for pensjonsgivende inntekt endpoint when status 200`() {
        pensjonsgivendeInntektMock.`stub pensjongivende inntekt`(INNTEKTSAAR, NORSK_PERSONIDENTIFIKATOR)

        val response = pgiClient.getPgi(
            pgiClient.createPgiRequest(INNTEKTSAAR, NORSK_PERSONIDENTIFIKATOR),
            HttpResponse.BodyHandlers.discarding()
        )

        assertEquals(200, response.statusCode())
    }

    @Test
    fun `should return response for pensjonsgivende inntekt endpoint when status is not 200`() {
        pensjonsgivendeInntektMock.`stub 401 from skatt`(INNTEKTSAAR, NORSK_PERSONIDENTIFIKATOR)

        val response = pgiClient.getPgi(
            pgiClient.createPgiRequest(INNTEKTSAAR, NORSK_PERSONIDENTIFIKATOR),
            HttpResponse.BodyHandlers.discarding()
        )

        assertEquals(401, response.statusCode())
    }

}

private fun parseJwt(bearerToken: String) = SignedJWT.parse(bearerToken.split("""Bearer """)[1])
private infix fun String.containsRegex(regex: String): Boolean = regex.toRegex().matches(this)