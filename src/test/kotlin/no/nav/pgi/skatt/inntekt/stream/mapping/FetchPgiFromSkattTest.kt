package no.nav.pgi.skatt.inntekt.stream.mapping

import no.nav.pgi.skatt.inntekt.mock.MaskinportenMock
import no.nav.pgi.skatt.inntekt.mock.MaskinportenMock.Companion.MASKINPORTEN_CLIENT_ENV_VARIABLES
import no.nav.pgi.skatt.inntekt.mock.PensjonsgivendeInntektMock
import no.nav.pgi.skatt.inntekt.mock.PensjonsgivendeInntektMock.Companion.PGI_CLIENT_ENV_VARIABLES
import no.nav.pgi.skatt.inntekt.skatt.PgiClient
import no.nav.samordning.pgi.schema.Hendelse
import no.nav.samordning.pgi.schema.HendelseMetadata
import org.apache.kafka.streams.kstream.ValueMapper
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class FetchPgiFromSkattTest {
    private val pgiClient: PgiClient = PgiClient(PGI_CLIENT_ENV_VARIABLES + MASKINPORTEN_CLIENT_ENV_VARIABLES)
    private val mapper: ValueMapper<Hendelse, PgiResponse> = FetchPgiFromSkatt(pgiClient)

    private val pensjonsgivendeInntektMock = PensjonsgivendeInntektMock()
    private val maskinportenMock = MaskinportenMock()

    @BeforeAll
    internal fun init() {
        maskinportenMock.`stub maskinporten token endpoint`()
    }

    @BeforeEach
    internal fun beforeEach() {
        pensjonsgivendeInntektMock.reset()
    }

    @AfterAll
    internal fun teardown() {
        pensjonsgivendeInntektMock.stop()
        maskinportenMock.stop()
    }

    @Test
    internal fun `should return response from skatt when 200 from skatt`() {
        pensjonsgivendeInntektMock.`stub pensjongivende inntekt`(GJELDER_PERIODE, IDENTIFIKATOR)
        val response = mapper.apply(HENDELSE)

        assertEquals(response.statusCode(), 200)
        assertNotNull(response.body())
    }

    @Test
    internal fun `should return response from skatt when skatt returns other status than 200`() {
        pensjonsgivendeInntektMock.`stub 401 from skatt`(GJELDER_PERIODE, IDENTIFIKATOR)
        val response = mapper.apply(HENDELSE)

        assertEquals(response.statusCode(), 401)
        assertNotNull(response.body())
    }

    companion object {
        private const val SEKVENSNUMMER = 1L
        private const val IDENTIFIKATOR = "12345678901"
        private const val GJELDER_PERIODE = "2019"

        private val HENDELSE = Hendelse(SEKVENSNUMMER, IDENTIFIKATOR, GJELDER_PERIODE, HendelseMetadata(0))
    }
}