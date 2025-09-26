package no.nav.pgi.skatt.inntekt.skatt

import no.nav.pensjon.opptjening.gcp.maskinporten.client.MaskinportenClient
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import no.nav.pgi.skatt.inntekt.util.getVal
import no.nav.pgi.skatt.inntekt.util.verifyEnvironmentVariables
import java.net.URI
import kotlin.div
import kotlin.math.log
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import no.nav.pgi.skatt.inntekt.ApplicationService
import no.nav.pgi.skatt.inntekt.stream.PGIStream
import org.slf4j.LoggerFactory

internal const val PENSJONGIVENDE_INNTEKT_HOST_ENV_KEY = "SKATT_INNTEKT_HOST"
internal const val SKATT_INNTEKT_PATH_ENV_KEY = "SKATT_INNTEKT_PATH"
internal const val MASKINPORTEN_WELL_KNOWN_URL_KEY = "MASKINPORTEN_WELL_KNOWN_URL"
internal const val MASKINPORTEN_CLIENT_ID_KEY = "MASKINPORTEN_CLIENT_ID"
internal const val MASKINPORTEN_CLIENT_JWK_KEY = "MASKINPORTEN_CLIENT_JWK"
internal const val MASKINPORTEN_SCOPES_KEY = "MASKINPORTEN_SCOPES"
internal const val MASKINPORTEN_JWT_EXPIRATION_TIME_IN_SECONDS_KEY = "MASKINPORTEN_JWT_EXPIRATION_TIME_IN_SECONDS"

class PgiClient(
    env: Map<String, String> = System.getenv(),
    val rateLimit: RateLimit = RateLimit(rate = 10000, timeInterval = 1.minutes)
) {

    private val maskinporten: MaskinportenClient = getMaskinporten(env)

    private fun getMaskinporten(env: Map<String, String>): MaskinportenClient {

        val requiredEnvKey = listOf(
            MASKINPORTEN_WELL_KNOWN_URL_KEY,
            MASKINPORTEN_CLIENT_ID_KEY,
            MASKINPORTEN_CLIENT_JWK_KEY,
            MASKINPORTEN_SCOPES_KEY
        )

        env.verifyEnvironmentVariables(requiredEnvKey)

        return MaskinportenClient.builder()
            .clientId(env[MASKINPORTEN_CLIENT_ID_KEY]!!)
            .privateKey(env[MASKINPORTEN_CLIENT_JWK_KEY]!!)
            .wellKnownUrl(env[MASKINPORTEN_WELL_KNOWN_URL_KEY]!!)
            .build()
    }


    private val httpClient: HttpClient = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_1_1)
        .build()
    private val skattHost = env.getVal(PENSJONGIVENDE_INNTEKT_HOST_ENV_KEY)
    private val skattPath = env.getVal(SKATT_INNTEKT_PATH_ENV_KEY)
    private val scope = env[MASKINPORTEN_SCOPES_KEY]!!

    internal fun <T> getPgi(
        httpRequest: HttpRequest,
        responseBodyHandler: HttpResponse.BodyHandler<T>
    ): HttpResponse<T> = rateLimit.limit { httpClient.send(httpRequest, responseBodyHandler) }


    internal fun createPgiRequest(
        inntektsaar: String,
        norskPersonidentifikator: String
    ): HttpRequest {

        val requestUrl = URI("$skattHost$skattPath/$inntektsaar/$norskPersonidentifikator")

        return HttpRequest.newBuilder()
            .uri(requestUrl)
            .GET()
            .setHeader("Authorization", "Bearer ${maskinporten.token(scope)}")
            .setHeader("Accept", "application/json")
            .build()
    }
}

class RateLimit(
    rate: Int,
    timeInterval: Duration,
) {
    val timeslot = timeInterval.inWholeMilliseconds / rate
    val timeslotNanos = timeInterval.inWholeNanoseconds / rate

    inline fun <reified T> limit(action: () -> T): T {
        val startTime = System.currentTimeMillis()
        val startNanos = System.nanoTime()

        val res = action()

        val endTime = System.currentTimeMillis()
        val elapsed = endTime - startTime

        val elapsedNanos = System.nanoTime() - startNanos

        return if (elapsed >= timeslot) {
            res
        } else {
            val sleepMillisNanos = (timeslotNanos - elapsedNanos) / 1_000_000
            val sleepMillis = timeslot - elapsed

            LOG.info("sleepMillis: $sleepMillis. sleepMillisNanos $sleepMillisNanos")

            Thread.sleep(sleepMillis)
            res
        }
    }

    companion object {
        val LOG = LoggerFactory.getLogger(RateLimit::class.java)
    }
}