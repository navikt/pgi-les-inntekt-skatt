package no.nav.pgi.skatt.inntekt

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import no.nav.pgi.skatt.inntekt.MaskinportenMock.Companion.MASKINPORTEN_ENV_VARIABLES
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.net.http.HttpResponse

private const val PORT = 8086
private const val PATH = "/testpath"
private const val URL = "http://localhost:$PORT$PATH"

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SkattClientTest {

    private val environment = mapOf("SKATT_URL" to URL)
    private val skattClient: SkattClient = SkattClient(environment + MASKINPORTEN_ENV_VARIABLES)
    private val skattMock = WireMockServer(PORT)
    private val maskinportenMock = MaskinportenMock()

    @BeforeAll
    internal fun init() {
        skattMock.start()
        maskinportenMock.`stub maskinporten token endpoint`()
    }

    @AfterAll
    internal fun teardown() {
        skattMock.stop()
        maskinportenMock.stop()
    }

    @Test
    fun `createGetRequest should add query parameters`() {
        val key1 = "key1"
        val value1 = "value1"
        val key2 = "key2"
        val value2 = "value2"

        skattMock.stubFor(WireMock.get(WireMock.urlPathEqualTo(PATH))
                .withQueryParams(mapOf(
                        key1 to WireMock.equalTo((value1)),
                        key2 to WireMock.equalTo((value2))
                ))
                .willReturn(WireMock.ok()))

        val response = skattClient.getPensjonsgivendeInntekter(skattClient.createGetRequest(mapOf(key1 to value1, key2 to value2)), HttpResponse.BodyHandlers.discarding())

        assertEquals(200, response.statusCode())
    }
}