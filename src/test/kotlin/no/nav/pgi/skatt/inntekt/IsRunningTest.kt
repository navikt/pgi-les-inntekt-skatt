package no.nav.pgi.skatt.inntekt

import no.nav.pgi.skatt.inntekt.common.KafkaTestEnvironment
import no.nav.pgi.skatt.inntekt.common.PlaintextStrategy
import no.nav.pgi.skatt.inntekt.mock.MaskinportenMock
import no.nav.pgi.skatt.inntekt.mock.PensjonsgivendeInntektMock
import no.nav.pgi.skatt.inntekt.skatt.PgiClient
import no.nav.pgi.skatt.inntekt.stream.KafkaConfig
import no.nav.pgi.skatt.inntekt.stream.PGIStream
import no.nav.pgi.skatt.inntekt.stream.PGITopology
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

// @TestInstance(TestInstance.Lifecycle.PER_CLASS)
/*
internal class IsRunningTest {
    private val kafkaTestEnvironment = KafkaTestEnvironment()
    private val pgiClient = PgiClient(PensjonsgivendeInntektMock.PGI_CLIENT_ENV_VARIABLES + MaskinportenMock.MASKINPORTEN_CLIENT_ENV_VARIABLES)
    private val kafkaConfig = KafkaConfig(kafkaTestEnvironment.testConfiguration(), PlaintextStrategy())
    private val pgiStream: PGIStream = PGIStream(kafkaConfig.streamProperties(), PGITopology(pgiClient))

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
        assertFalse(PGIStream(kafkaConfig.streamProperties(), PGITopology(pgiClient)).isRunning())
    }

    @Test
    fun `isRunning() should return true when app is started`() {
        assertTrue(pgiStream.isRunning())
    }
}
 */