package no.nav.pgi.skatt.inntekt.mock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import no.nav.pensjon.samhandling.maskinporten.*
import java.util.*

private const val PORT = 8096
private const val TOKEN_PATH = "/token"
internal const val MASKINPORTEN_MOCK_HOST = "http://localhost:$PORT"

internal class MaskinportenMock {
    private var mock = WireMockServer(PORT).also { it.start() }
    private val privateKey: RSAKey = RSAKeyGenerator(2048).keyID("123").generate()

    internal fun stop() {
        mock.stop()
    }

    internal fun `stub maskinporten token endpoint`() {
        mock.stubFor(WireMock.post(WireMock.urlPathEqualTo(TOKEN_PATH))
                .willReturn(WireMock.ok("""{
                      "access_token" : "${createMaskinportenToken()}",
                      "token_type" : "Bearer",
                      "expires_in" : 599,
                      "scope" : "difitest:test1"
                    }
                """)))
    }

    private fun createMaskinportenToken(): String {
        val claimsSet = JWTClaimsSet.Builder()
                .subject("alice")
                .issuer("https://c2id.com")
                .expirationTime(Date(Date().getTime() + (60 * 1000)))
                .build()
        val signedJWT = SignedJWT(
                JWSHeader.Builder(JWSAlgorithm.RS256).keyID(privateKey.getKeyID()).build(),
                claimsSet)
        val signer: JWSSigner = RSASSASigner(privateKey)
        signedJWT.sign(signer)
        return signedJWT.serialize()
    }

    companion object {
        val MASKINPORTEN_ENV_VARIABLES: Map<String, String> = mapOf(
                SCOPE_ENV_KEY to "testScope",
                CLIENT_ID_ENV_KEY to "testClient",
                VALID_IN_SECONDS_ENV_KEY to "120",
                PRIVATE_JWK_ENV_KEY to RSAKeyGenerator(2048).keyID("123").generate().toJSONString(),
                MASKINPORTEN_TOKEN_HOST_ENV_KEY to MASKINPORTEN_MOCK_HOST)
    }

}