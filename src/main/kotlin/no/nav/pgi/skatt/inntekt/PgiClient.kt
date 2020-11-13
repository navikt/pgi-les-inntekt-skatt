package no.nav.pgi.skatt.inntekt

import no.nav.pensjon.samhandling.env.getVal
import no.nav.pensjon.samhandling.maskinporten.Maskinporten
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import javax.ws.rs.core.UriBuilder

internal const val PENSJONGIVENDE_INNTEKT_URL_ENV_KEY = "SKATT_INNTEKT_URL"

class PgiClient(env: Map<String, String> = System.getenv()) {
    private val maskinporten: Maskinporten = Maskinporten(env)
    private val httpClient: HttpClient = HttpClient.newHttpClient()
    private val skattUrl = env.getVal(PENSJONGIVENDE_INNTEKT_URL_ENV_KEY)

    internal fun <T> getPensjonsgivendeInntekter(httpRequest: HttpRequest, responseBodyHandler: HttpResponse.BodyHandler<T>): HttpResponse<T> {
        val response = httpClient.send(httpRequest, responseBodyHandler)
        return when {
            response.statusCode() == 200 -> response
            else -> throw PensjonsgivendeInntektClientException("Call to pgi failed with code: ${response.statusCode()} and body: ${response.body()}")
        }
    }

    internal fun createPensjonsgivendeInntekterRequest(inntektsaar: String, norskPersonidentifikator: String) = HttpRequest.newBuilder()
            .uri(UriBuilder.fromPath(skattUrl).path(inntektsaar).path(norskPersonidentifikator).build())
            .GET()
            .setHeader("Authorization", "Bearer ${maskinporten.token}")
            .build()
}

class PensjonsgivendeInntektClientException(message: String) : RuntimeException(message)