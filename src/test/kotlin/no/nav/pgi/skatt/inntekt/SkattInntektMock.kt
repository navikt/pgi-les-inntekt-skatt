package no.nav.pgi.skatt.inntekt

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock

private const val SKATT_INNTEKT_PORT = 8085
private const val SKATT_INNTEKT_PATH = "/api/skatteoppgjoer/ekstern/grunnlag-pgi/hendelse/start"
internal const val SKATT_INNTEKT_URL = "http://localhost:$SKATT_INNTEKT_PORT$SKATT_INNTEKT_PATH"

//https://skatteetaten.github.io/datasamarbeid-api-dokumentasjon/reference_pgi.html
internal class SkattInntektMock {

    private val skattApiMock = WireMockServer(SKATT_INNTEKT_PORT)

    init {
        skattApiMock.start()
    }

    internal fun `stub inntekt fra skatt`() {
        skattApiMock.stubFor(WireMock.get(WireMock.urlPathEqualTo(SKATT_INNTEKT_PATH))
                .willReturn(WireMock.okJson(pgiJson())))
    }

    internal fun stop() {
        skattApiMock.stop()
    }

    private fun pgiJson() = "{\n" +
            "  \"personidentifikator\": \"01234567890\",\n" +
            "  \"inntektsaar\": \"2017\",\n" +
            "  \"pensjonsgivendeInntekt\": 100000,\n" +
            "  \"pensjonsgivendeInntektFastland\": 50000,\n" +
            "  \"pensjonsgivendeInntektSvalbard\": 50000\n" +
            "}"

}