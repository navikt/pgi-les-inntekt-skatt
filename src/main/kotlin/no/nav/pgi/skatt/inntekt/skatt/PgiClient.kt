package no.nav.pgi.skatt.inntekt.skatt

import no.nav.pensjon.opptjening.gcp.maskinporten.client.MaskinportenClient
import no.nav.pensjon.opptjening.gcp.maskinporten.client.config.MaskinportenEnvVariableConfigCreator.Companion.createMaskinportenConfig
import no.nav.pensjon.samhandling.env.getVal
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import javax.ws.rs.core.UriBuilder
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

internal const val PENSJONGIVENDE_INNTEKT_HOST_ENV_KEY = "SKATT_INNTEKT_HOST"
internal const val SKATT_INNTEKT_PATH_ENV_KEY = "SKATT_INNTEKT_PATH"

class PgiClient(env: Map<String, String> = System.getenv(), val rateLimit: RateLimit = RateLimit(rate = 500, timeInterval = 5.minutes)) {
    private val maskinporten: MaskinportenClient = MaskinportenClient(createMaskinportenConfig(env))
    private val httpClient: HttpClient = HttpClient.newHttpClient()
    private val skattHost = env.getVal(PENSJONGIVENDE_INNTEKT_HOST_ENV_KEY)
    private val skattPath = env.getVal(SKATT_INNTEKT_PATH_ENV_KEY)

    internal fun <T> getPgi(
        httpRequest: HttpRequest,
        responseBodyHandler: HttpResponse.BodyHandler<T>
    ): HttpResponse<T> = rateLimit.limit { httpClient.send(httpRequest, responseBodyHandler) }


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

class RateLimit(
    rate: Int,
    timeInterval: Duration,
) {
    val timeslot = timeInterval.inWholeMilliseconds / rate

    inline fun <reified T> limit(action: () -> T): T {
        val startTime = System.currentTimeMillis()

        val res = action()

        val endTime = System.currentTimeMillis()
        val elapsed = endTime - startTime

        return if (elapsed >= timeslot) {
            res
        } else {
            Thread.sleep(timeslot - elapsed)
            res
        }
    }
}