package no.nav.pgi.skatt.inntekt

import no.nav.pgi.skatt.inntekt.common.KafkaTestEnvironment
import no.nav.pgi.skatt.inntekt.common.PlaintextStrategy
import no.nav.pgi.skatt.inntekt.mock.MaskinportenMock
import no.nav.pgi.skatt.inntekt.mock.PensjonsgivendeInntektMock
import no.nav.pgi.skatt.inntekt.skatt.PgiClient
import no.nav.pgi.skatt.inntekt.stream.KafkaConfig
import no.nav.pgi.skatt.inntekt.stream.PGIStream
import no.nav.pgi.skatt.inntekt.stream.PGITopology
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
        val pgiStream = PGIStream(kafkaConfig.streamProperties(), PGITopology(pgiClient))
        assertThat(pgiStream.isRunning()).isFalse()
    }

    @Test
    fun `isRunning() should return true when app is started`() {
        assertThat(pgiStream.isRunning()).isTrue()
    }
}