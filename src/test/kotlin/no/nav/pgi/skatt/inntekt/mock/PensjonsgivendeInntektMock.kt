package no.nav.pgi.skatt.inntekt.mock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock

private const val PORT = 8097
private const val PENSJONSGIVENDE_INNTEKT_PATH = "/api/skatteoppgjoer/ekstern/grunnlag-pgi"
internal const val PENSJONGIVENDE_INNTEKT_MOCK_URL = "http://localhost:$PORT$PENSJONSGIVENDE_INNTEKT_PATH"

class PensjonsgivendeInntektMock {
    private var mock = WireMockServer(PORT).also { it.start() }

    internal fun reset() {
        mock.resetAll()
    }

    internal fun stop() {
        mock.stop()
    }

    internal fun `stub pensjongivende inntekt`() {
        mock.stubFor(WireMock.get(WireMock.urlPathEqualTo("""WireMock.urlMatching(PENSJONSGIVENDE_INNTEKT_PATH)"""))
                .willReturn(WireMock.ok(createResponse("2019", "12345678901"))))
    }

    internal fun `stub pensjongivende inntekt`(inntektsaar: String, norskPersonidentifikator: String) {
        mock.stubFor(WireMock.get(WireMock.urlPathEqualTo("""$PENSJONSGIVENDE_INNTEKT_PATH/$inntektsaar/$norskPersonidentifikator"""))
                .willReturn(WireMock.ok(createResponse(inntektsaar, norskPersonidentifikator))))
    }

    internal fun `stub 401 fra skatt`() {
        mock.stubFor(WireMock.get(WireMock.urlMatching(PENSJONSGIVENDE_INNTEKT_PATH)).willReturn(WireMock.unauthorized()))
    }

    private fun createResponse(inntektsaar: String, norskPersonidentifikator: String) =
            """{
                  "norskPersonidentifikator": "$norskPersonidentifikator",
                  "inntektsaar": ${inntektsaar.toInt()},
                  "pensjonsgivendeInntekt": [
                    {
                      "skatteordning": "FASTLAND",
                      "datoForFastsetting": "2020-11-02",
                      "pensjonsgivendeInntektAvLoennsinntekt": 9656805,
                      "pensjonsgivendeInntektAvLoennsinntektBarePensjonsdel": 8124969,
                      "pensjonsgivendeInntektAvNaeringsinntekt": 6171061,
                      "pensjonsgivendeInntektAvNaeringsinntektFraFiskeFangstEllerFamiliebarnehage": 2407046
                    },
                    {
                      "skatteordning": "KILDESKATT_PAA_LOENN",
                      "datoForFastsetting": "2020-11-02",
                      "pensjonsgivendeInntektAvLoennsinntekt": 1981769,
                      "pensjonsgivendeInntektAvLoennsinntektBarePensjonsdel": 2973637,
                      "pensjonsgivendeInntektAvNaeringsinntekt": 8331534,
                      "pensjonsgivendeInntektAvNaeringsinntektFraFiskeFangstEllerFamiliebarnehage": 187334
                    },
                    {
                      "skatteordning": "SVALBARD",
                      "datoForFastsetting": "2020-11-02",
                      "pensjonsgivendeInntektAvLoennsinntekt": 4889051,
                      "pensjonsgivendeInntektAvLoennsinntektBarePensjonsdel": 2915688,
                      "pensjonsgivendeInntektAvNaeringsinntekt": 6764601,
                      "pensjonsgivendeInntektAvNaeringsinntektFraFiskeFangstEllerFamiliebarnehage": 3050771
                    }
                  ]
                }"""


}