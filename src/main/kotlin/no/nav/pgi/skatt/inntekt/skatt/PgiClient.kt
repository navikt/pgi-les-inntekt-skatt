package no.nav.pgi.skatt.inntekt.skatt

import no.nav.pensjon.opptjening.gcp.maskinporten.client.MaskinportenClient
import no.nav.pensjon.opptjening.gcp.maskinporten.client.config.MaskinportenEnvVariableConfigCreator.Companion.createMaskinportenConfig
import no.nav.pensjon.samhandling.env.getVal
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import javax.ws.rs.core.UriBuilder

internal const val PENSJONGIVENDE_INNTEKT_HOST_ENV_KEY = "SKATT_INNTEKT_HOST"
internal const val VERSJON = "v1"
internal const val RETTIGHETSPAKKE = "navPensjonopptjening"
internal const val SKATT_INNTEKT_PATH_ENV_KEY = "SKATT_INNTEKT_PATH"

class PgiClient(env: Map<String, String> = System.getenv()) {
    private val maskinporten: MaskinportenClient = MaskinportenClient(createMaskinportenConfig(env))
    private val httpClient: HttpClient = HttpClient.newHttpClient()
    private val skattHost = env.getVal(PENSJONGIVENDE_INNTEKT_HOST_ENV_KEY)
    private val skattPath = env.getVal(SKATT_INNTEKT_PATH_ENV_KEY)

    internal fun <T> getPgi(httpRequest: HttpRequest, responseBodyHandler: HttpResponse.BodyHandler<T>):
            HttpResponse<T> = httpClient.send(httpRequest, responseBodyHandler)

    internal fun createPgiRequest(inntektsaar: String, norskPersonidentifikator: String) =
        HttpRequest.newBuilder().uri(
            UriBuilder.fromPath(skattHost)
                .path(skattPath)
                .path(inntektsaar)
                .path(norskPersonidentifikator)
                .build()
        )
            .GET()
            .setHeader("Authorization", "Bearer ${maskinporten.tokenString}")
            .setHeader("Accept", "application/json")
            .build()
}