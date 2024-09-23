package no.nav.pgi.skatt.inntekt.stream.mapping

import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import no.nav.pgi.domain.Hendelse
import no.nav.pgi.domain.HendelseMetadata
import no.nav.pgi.skatt.inntekt.mock.MaskinportenMock
import no.nav.pgi.skatt.inntekt.mock.MaskinportenMock.Companion.MASKINPORTEN_CLIENT_ENV_VARIABLES
import no.nav.pgi.skatt.inntekt.mock.PensjonsgivendeInntektMock
import no.nav.pgi.skatt.inntekt.mock.PensjonsgivendeInntektMock.PGI_CLIENT_ENV_VARIABLES
import no.nav.pgi.skatt.inntekt.mock.PensjonsgivendeInntektMock.PORT
import no.nav.pgi.skatt.inntekt.mock.PensjonsgivendeInntektMock.`stub 401 from skatt`
import no.nav.pgi.skatt.inntekt.mock.PensjonsgivendeInntektMock.`stub pensjongivende inntekt`
import no.nav.pgi.skatt.inntekt.skatt.PgiClient
import org.apache.kafka.streams.kstream.ValueMapper
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.extension.RegisterExtension

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class FetchPgiFromSkattTest {
    private val pgiClient: PgiClient = PgiClient(PGI_CLIENT_ENV_VARIABLES + MASKINPORTEN_CLIENT_ENV_VARIABLES)
    private val mapper: ValueMapper<Hendelse, PgiResponse> = FetchPgiFromSkatt(pgiClient)

    private val maskinportenMock = MaskinportenMock()

    @BeforeAll
    internal fun init() {
        maskinportenMock.`stub maskinporten token endpoint`()
    }

    @BeforeEach
    internal fun beforeEach() {
    }

    @AfterAll
    internal fun teardown() {
        maskinportenMock.stop()
    }

    @Test
    internal fun `should return response from skatt when 200 from skatt`() {
        mock.`stub pensjongivende inntekt`(GJELDER_PERIODE, IDENTIFIKATOR)
        val response = mapper.apply(HENDELSE)

        assertEquals(response.statusCode(), 200)
        assertNotNull(response.body())
    }

    @Test
    internal fun `should return response from skatt when skatt returns other status than 200`() {
        mock.`stub 401 from skatt`(GJELDER_PERIODE, IDENTIFIKATOR)
        val response = mapper.apply(HENDELSE)

        assertEquals(response.statusCode(), 401)
        assertNotNull(response.body())
    }

    companion object {
        private const val SEKVENSNUMMER = 1L
        private const val IDENTIFIKATOR = "12345678901"
        private const val GJELDER_PERIODE = "2019"

        private val HENDELSE = Hendelse(SEKVENSNUMMER, IDENTIFIKATOR, GJELDER_PERIODE, HendelseMetadata(0))

        @JvmStatic
        @RegisterExtension
        private val mock =
            WireMockExtension.newInstance()
                .options(
                    WireMockConfiguration.wireMockConfig().port(PORT)
                        .templatingEnabled(false)
                )
                .build()!!
    }
}