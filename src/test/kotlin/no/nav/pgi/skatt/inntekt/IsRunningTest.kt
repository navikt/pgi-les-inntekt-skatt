package no.nav.pgi.skatt.inntekt

/*

TODO: Tester nesten ingenting og krever mye oppsett - kommenterer ut for nå

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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka

@SpringBootTest
@EmbeddedKafka
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class IsRunningTest {

    @Autowired
    lateinit var embeddedKafka: EmbeddedKafkaBroker

    private val pgiClient =
        PgiClient(PensjonsgivendeInntektMock.PGI_CLIENT_ENV_VARIABLES + MaskinportenMock.MASKINPORTEN_CLIENT_ENV_VARIABLES)

    private fun kafkaConfig(): KafkaConfig {
        return KafkaConfig(testConfiguration(), PlaintextStrategy())
    }

    // TODO: bli kvitt denne?
    private fun testConfiguration() = mapOf(
        KafkaConfig.BOOTSTRAP_SERVERS to embeddedKafka.brokersAsString,
        KafkaConfig.SCHEMA_REGISTRY_USERNAME to "mrOpenSource",
        KafkaConfig.SCHEMA_REGISTRY_PASSWORD to "opensourcedPassword",
    )


    @Test
    fun `isRunning() should return false when stream is not started`() {
        val pgiStream = PGIStream(kafkaConfig().streamProperties(), PGITopology(pgiClient))
        assertThat(pgiStream.isRunning()).isFalse()
        pgiStream.close()
    }

    @Test
    fun `isRunning() should return true when app is started`() {
        val pgiStream = PGIStream(kafkaConfig().streamProperties(), PGITopology(pgiClient))
        pgiStream.start()
        assertThat(pgiStream.isRunning()).isTrue()
        pgiStream.close()
    }
}
 */