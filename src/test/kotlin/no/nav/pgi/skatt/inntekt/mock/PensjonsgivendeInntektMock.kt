package no.nav.pgi.skatt.inntekt.mock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.matching
import com.github.tomakehurst.wiremock.client.WireMock.ok
import com.github.tomakehurst.wiremock.client.WireMock.serverError
import com.github.tomakehurst.wiremock.client.WireMock.unauthorized
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer
import no.nav.pgi.skatt.inntekt.skatt.PENSJONGIVENDE_INNTEKT_HOST_ENV_KEY
import no.nav.pgi.skatt.inntekt.skatt.RETTIGHETSPAKKE
import no.nav.pgi.skatt.inntekt.skatt.SKATT_INNTEKT_PATH_ENV_KEY
import no.nav.pgi.skatt.inntekt.skatt.VERSJON
import no.nav.samordning.pgi.schema.Hendelse


class PensjonsgivendeInntektMock {
    private var config = wireMockConfig().port(PORT)
        .extensions(ResponseTemplateTransformer(false))
    private var mock = WireMockServer(config).also { it.start() }

    internal fun reset() {
        mock.resetAll()
    }

    internal fun stop() {
        mock.stop()
    }

    internal fun callsToMock() = mock.serveEvents.requests.size

    internal fun `stub pensjongivende inntekt endpoint`() {
        mock.stubFor(
            get(urlPathMatching("/$VERSJON/$RETTIGHETSPAKKE$YEAR_FNR"))
                .atPriority(10)
                .withName("ape")
                .withHeader("Authorization", matching(TOKEN))
                .willReturn(
                    aResponse()
                        .withBody(createResponse("{{request.path.[3]}}", "{{request.path.[4]}}"))
                        .withTransformer("response-template", "mock", "mock")
                        .withStatus(200)
                )
        )
    }

    internal fun `stub pensjongivende inntekt`(inntektsaar: String, norskPersonidentifikator: String) {
        mock.stubFor(
            get(urlPathEqualTo("""/$VERSJON/$RETTIGHETSPAKKE/$inntektsaar/$norskPersonidentifikator"""))
                .atPriority(9)
                .willReturn(ok(createResponse(inntektsaar, norskPersonidentifikator)))
        )
    }

    internal fun `stub error code from skatt`(hendelse: Hendelse, errorCode: String) {
        mock.stubFor(
            get(urlPathMatching("/$VERSJON/$RETTIGHETSPAKKE/${hendelse.getGjelderPeriode()}/${hendelse.getIdentifikator()}"))
                .atPriority(1)
                .willReturn(serverError().withBody(errorCode))
        )
    }

    internal fun `stub 401 from skatt`(inntektsaar: String, norskPersonidentifikator: String) {
        mock.stubFor(
            get(urlPathMatching("/$VERSJON/$RETTIGHETSPAKKE/$inntektsaar/$norskPersonidentifikator"))
                .atPriority(1)
                .willReturn(unauthorized())
        )
    }

    private fun createResponse(inntektsaar: String, norskPersonidentifikator: String) =
        """{
                  "norskPersonidentifikator": "$norskPersonidentifikator",
                  "inntektsaar": ${inntektsaar},
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


    companion object {
        private const val PORT = 8097
        private const val YEAR_FNR = """/[0-9]{4}/[0-9]{11}"""
        private const val TOKEN = """.*\..*\..*"""

        internal val PGI_CLIENT_ENV_VARIABLES = mapOf(
            PENSJONGIVENDE_INNTEKT_HOST_ENV_KEY to "http://localhost:$PORT",
            SKATT_INNTEKT_PATH_ENV_KEY to "/v1/navPensjonopptjening",
        )
    }
}