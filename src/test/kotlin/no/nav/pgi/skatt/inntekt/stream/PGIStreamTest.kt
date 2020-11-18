package no.nav.pgi.skatt.inntekt.stream

import no.nav.pgi.skatt.inntekt.KafkaTestEnvironment
import no.nav.pgi.skatt.inntekt.PENSJONGIVENDE_INNTEKT_HOST_ENV_KEY
import no.nav.pgi.skatt.inntekt.PgiClient
import no.nav.pgi.skatt.inntekt.PlaintextStrategy
import no.nav.pgi.skatt.inntekt.mock.MaskinportenMock
import no.nav.pgi.skatt.inntekt.mock.PENSJONGIVENDE_INNTEKT_HOST
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class PGIStreamTest {
    private val kafkaTestEnvironment = KafkaTestEnvironment()
    private val pgiClient = PgiClient(mapOf(PENSJONGIVENDE_INNTEKT_HOST_ENV_KEY to PENSJONGIVENDE_INNTEKT_HOST) + MaskinportenMock.MASKINPORTEN_ENV_VARIABLES)
    private val kafkaConfig = KafkaConfig(kafkaTestEnvironment.testConfiguration(), PlaintextStrategy())
    private val pgiStream: PGIStream = PGIStream(kafkaConfig.streamConfig(), pgiClient)

    @BeforeAll
    fun beforeAll() {
        pgiStream.start()
    }

    @AfterAll
    fun afterAll() {
        pgiStream.close()
    }

    @Test
    fun `isRunning() should return false when stream is not started`() {
        assertFalse(PGIStream(kafkaConfig.streamConfig(), pgiClient).isRunning())
    }

    @Test
    fun `isRunning() should return true when app is started`() {
        assertTrue(pgiStream.isRunning())
    }
}