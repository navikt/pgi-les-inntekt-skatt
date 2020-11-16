package no.nav.pgi.skatt.inntekt

import no.nav.pgi.skatt.inntekt.mock.MaskinportenMock
import no.nav.pgi.skatt.inntekt.mock.MaskinportenMock.Companion.MASKINPORTEN_ENV_VARIABLES
import no.nav.pgi.skatt.inntekt.mock.PENSJONGIVENDE_INNTEKT_HOST
import no.nav.pgi.skatt.inntekt.mock.PensjonsgivendeInntektMock
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.net.http.HttpResponse

//TODO Error håndtering på exception nivo
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PgiClientTest {

    private val environment = mapOf(PENSJONGIVENDE_INNTEKT_HOST_ENV_KEY to PENSJONGIVENDE_INNTEKT_HOST)
    private val pgiClient: PgiClient = PgiClient(environment + MASKINPORTEN_ENV_VARIABLES)
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

        val response = pgiClient.getPensjonsgivendeInntekter(pgiClient.createPensjonsgivendeInntekterRequest(inntektsaar, norskPersonidentifikator), HttpResponse.BodyHandlers.discarding())

        assertEquals(200, response.statusCode())
    }
}