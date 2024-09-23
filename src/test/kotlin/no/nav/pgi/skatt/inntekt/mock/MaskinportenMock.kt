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
import java.util.*


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
            .expirationTime(Date(Date().time + (60 * 1000)))
            .build()
        val signedJWT = SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.RS256).keyID(privateKey.keyID).build(),
            claimsSet
        )
        val signer: JWSSigner = RSASSASigner(privateKey)
        signedJWT.sign(signer)
        return signedJWT.serialize()
    }

    companion object {
        private const val PORT = 8096
        private const val TOKEN_PATH = "/token"
        private const val MASKINPORTEN_MOCK_HOST = "http://localhost:$PORT"

        internal val MASKINPORTEN_WELL_KNOWN_URL_KEY = "MASKINPORTEN_WELL_KNOWN_URL"
        internal val MASKINPORTEN_CLIENT_ID_KEY = "MASKINPORTEN_CLIENT_ID"
        internal val MASKINPORTEN_CLIENT_JWK_KEY = "MASKINPORTEN_CLIENT_JWK"
        internal val MASKINPORTEN_SCOPES_KEY = "MASKINPORTEN_SCOPES"
        internal val MASKINPORTEN_JWT_EXPIRATION_TIME_IN_SECONDS_KEY = "MASKINPORTEN_JWT_EXPIRATION_TIME_IN_SECONDS"

        val MASKINPORTEN_CLIENT_ENV_VARIABLES = mapOf(
            MASKINPORTEN_SCOPES_KEY to "testScope",
            MASKINPORTEN_CLIENT_ID_KEY to "testClient",
            MASKINPORTEN_JWT_EXPIRATION_TIME_IN_SECONDS_KEY to "120",
            MASKINPORTEN_CLIENT_JWK_KEY to RSAKeyGenerator(2048).keyID("123").generate().toJSONString(),
            MASKINPORTEN_WELL_KNOWN_URL_KEY to MASKINPORTEN_MOCK_HOST
        )
    }
}