package no.nav.pgi.skatt.inntekt.mock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import no.nav.pgi.domain.Hendelse
import no.nav.pgi.skatt.inntekt.skatt.PENSJONGIVENDE_INNTEKT_HOST_ENV_KEY
import no.nav.pgi.skatt.inntekt.skatt.SKATT_INNTEKT_PATH_ENV_KEY
import org.junit.jupiter.api.extension.RegisterExtension


object PensjonsgivendeInntektMock {

    const val PORT = 8097
    private const val YEAR_FNR = """/[0-9]{4}/[0-9]{11}"""
    private const val TOKEN = """.*\..*\..*"""

    internal const val HOST = "http://localhost:$PORT"
    internal const val VERSJON_RETTIGHETSPAKKE = "/v1/navPensjonOpptjening"

    internal val PGI_CLIENT_ENV_VARIABLES = mapOf(
        PENSJONGIVENDE_INNTEKT_HOST_ENV_KEY to HOST,
        SKATT_INNTEKT_PATH_ENV_KEY to VERSJON_RETTIGHETSPAKKE,
    )

    fun WireMockExtension.callsToMock() = this.serveEvents.requests.size

    internal fun WireMockExtension.`stub pensjongivende inntekt endpoint`() {
        this.stubFor(
            get(urlPathMatching("$VERSJON_RETTIGHETSPAKKE$YEAR_FNR"))
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

    internal fun WireMockExtension.`stub pensjongivende inntekt`(
        inntektsaar: String,
        norskPersonidentifikator: String
    ) {
        this.stubFor(
            get(urlPathEqualTo("""$VERSJON_RETTIGHETSPAKKE/$inntektsaar/$norskPersonidentifikator"""))
                .atPriority(9)
                .willReturn(ok(createResponse(inntektsaar, norskPersonidentifikator)))
        )
    }

    internal fun WireMockExtension.`stub error code from skatt`(hendelse: Hendelse, errorCode: String) {
        this.stubFor(
            get(urlPathMatching("$VERSJON_RETTIGHETSPAKKE/${hendelse.gjelderPeriode}/${hendelse.identifikator}"))
                .atPriority(1)
                .willReturn(serverError().withBody(errorCode))
        )
    }

    internal fun WireMockExtension.`stub 401 from skatt`(inntektsaar: String, norskPersonidentifikator: String) {
        this.stubFor(
            get(urlPathMatching("$VERSJON_RETTIGHETSPAKKE/$inntektsaar/$norskPersonidentifikator"))
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
}
