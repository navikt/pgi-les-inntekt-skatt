package no.nav.pgi.skatt.inntekt

import no.nav.pensjon.samhandling.env.getVal
import no.nav.pensjon.samhandling.maskinporten.Maskinporten
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class SkattClient(env: Map<String, String> = System.getenv()) {
    private val maskinporten: Maskinporten = Maskinporten(env)
    private val httpClient: HttpClient = HttpClient.newHttpClient()
    private val skattUrl = env.getVal("SKATT_URL")

    internal fun <T> send(httpRequest: HttpRequest, responseBodyHandler: HttpResponse.BodyHandler<T>): HttpResponse<T> =
            httpClient.send(httpRequest, responseBodyHandler)

    internal fun createGetRequest(queryParameters: Map<String, Any> = emptyMap()) = HttpRequest.newBuilder()
            .uri(URI.create(skattUrl + queryParameters.createQueryString()))
            .GET()
            .setHeader("Authorization", "Bearer ${maskinporten.token}")
            .build()
}

internal fun Map<String, Any>.createQueryString() =
        if (isEmpty()) "" else keys.joinToString(prefix = "?", separator = "&") { "$it=${get(it).toString()}" }