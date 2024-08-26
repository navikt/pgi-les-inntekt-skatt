package no.nav.pgi.skatt.inntekt.stream

import no.nav.pensjon.samhandling.env.getVal
import org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG
import org.apache.kafka.common.serialization.Serdes.StringSerde
import org.apache.kafka.streams.StreamsConfig.*
import java.util.*

internal const val STREAM_APPLICATION_ID = "pgi-les-inntekt-skatt-asfas2131d"
internal const val PGI_INNTEKT_TOPIC = "pensjonopptjening.privat-pgi-inntekt"
internal const val PGI_HENDELSE_TOPIC = "pensjonopptjening.privat-pgi-hendelse"

internal class KafkaConfig(environment: Map<String, String> = System.getenv(), private val securityStrategy: SecurityStrategy = SslStrategy()) {
    private val bootstrapServers = environment.getVal(BOOTSTRAP_SERVERS)

    internal fun streamProperties(): Properties = Properties().apply {
        put(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        put(DEFAULT_KEY_SERDE_CLASS_CONFIG, StringSerde::class.java)
        put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, StringSerde::class.java)
        put(APPLICATION_ID_CONFIG, STREAM_APPLICATION_ID)
        put(AUTO_OFFSET_RESET_CONFIG, "earliest")
        putAll(securityStrategy.securityConfig())
    }

    internal companion object EnvironmentKeys {
        const val BOOTSTRAP_SERVERS = "KAFKA_BROKERS"
        const val SCHEMA_REGISTRY = "KAFKA_SCHEMA_REGISTRY"
        const val SCHEMA_REGISTRY_USERNAME = "KAFKA_SCHEMA_REGISTRY_USER"
        const val SCHEMA_REGISTRY_PASSWORD = "KAFKA_SCHEMA_REGISTRY_PASSWORD"
    }

    internal interface SecurityStrategy {
        fun securityConfig(): Map<String, String>
    }
}
