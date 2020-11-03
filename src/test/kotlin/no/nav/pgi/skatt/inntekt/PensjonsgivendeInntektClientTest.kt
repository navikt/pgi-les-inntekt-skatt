package no.nav.pgi.skatt.inntekt

import no.nav.pgi.skatt.inntekt.mock.MaskinportenMock
import no.nav.pgi.skatt.inntekt.mock.MaskinportenMock.Companion.MASKINPORTEN_ENV_VARIABLES
import no.nav.pgi.skatt.inntekt.mock.PENSJONGIVENDE_INNTEKT_MOCK_URL
import no.nav.pgi.skatt.inntekt.mock.PensjonsgivendeInntektMock
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.net.http.HttpResponse


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PensjonsgivendeInntektClientTest {

    private val environment = mapOf(PENSJONGIVENDE_INNTEKT_URL_ENV_KEY to PENSJONGIVENDE_INNTEKT_MOCK_URL)
    private val pensjonsgivendeInntektClient: PensjonsgivendeInntektClient = PensjonsgivendeInntektClient(environment + MASKINPORTEN_ENV_VARIABLES)
    private val pensjonsgivendeInntektMock = PensjonsgivendeInntektMock()
    private val maskinportenMock = MaskinportenMock()

    @BeforeAll
    internal fun init() {
        maskinportenMock.`stub maskinporten token endpoint`()
    }

    @AfterAll
    internal fun teardown() {
        pensjonsgivendeInntektMock.stop()
        maskinportenMock.stop()
    }

    @Test
    fun `createGetRequest should add query parameters`() {
        val inntektsaar = "2018"
        val norskPersonidentifikator = "12345678901"

        pensjonsgivendeInntektMock.`stub pensjongivende inntekt`(inntektsaar,norskPersonidentifikator)

        val response = pensjonsgivendeInntektClient.getPensjonsgivendeInntekter(pensjonsgivendeInntektClient.createPensjonsgivendeInntekterRequest(inntektsaar, norskPersonidentifikator), HttpResponse.BodyHandlers.discarding())

        assertEquals(200, response.statusCode())
    }
}