package no.nav.pgi.skatt.inntekt.stream

import no.nav.pgi.skatt.inntekt.PlaintextStrategy

internal class PGIStreamTest {
    private val kafkaConfig = KafkaConfig(getKafkaTestEnv(), PlaintextStrategy())
    //val testDriver: TopologyTestDriver = TopologyTestDriver(kafkaConfig.streamConfig())

    private fun getKafkaTestEnv() =
            mapOf(KafkaConfig.BOOTSTRAP_SERVERS to "test",
                    KafkaConfig.SCHEMA_REGISTRY_USERNAME to "test",
                    KafkaConfig.SCHEMA_REGISTRY_PASSWORD to "test",
                    KafkaConfig.SCHEMA_REGISTRY to "test")

}





