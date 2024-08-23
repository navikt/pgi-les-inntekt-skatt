package no.nav.pgi.skatt.inntekt.stream

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.*
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import no.nav.pensjon.samhandling.env.getVal
import org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.streams.StreamsConfig.*
import java.util.*

internal const val STREAM_APPLICATION_ID = "pgi-les-inntekt-skatt-asfas2131d"
internal const val PGI_INNTEKT_TOPIC = "pensjonopptjening.privat-pgi-inntekt"
internal const val PGI_HENDELSE_TOPIC = "pensjonopptjening.privat-pgi-hendelse"

internal class KafkaConfig(environment: Map<String, String> = System.getenv(), private val securityStrategy: SecurityStrategy = SslStrategy()) {
    private val bootstrapServers = environment.getVal(BOOTSTRAP_SERVERS)
    private val schemaRegUsername = environment.getVal(SCHEMA_REGISTRY_USERNAME)
    private val schemaRegPassword = environment.getVal(SCHEMA_REGISTRY_PASSWORD)
    private val schemaRegistryUrl = environment.getVal(SCHEMA_REGISTRY)

    internal fun streamProperties(): Properties = Properties().apply {
        put(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        put(DEFAULT_KEY_SERDE_CLASS_CONFIG, StringSerializer::class.java)
        put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, StringSerializer::class.java)
        put(APPLICATION_ID_CONFIG, STREAM_APPLICATION_ID)
        put(AUTO_OFFSET_RESET_CONFIG, "earliest")
        putAll(securityStrategy.securityConfig())
        putAll(schemaRegistryConfig())
    }

    private fun schemaRegistryConfig() = mapOf(
            BASIC_AUTH_CREDENTIALS_SOURCE to "USER_INFO",
            USER_INFO_CONFIG to "$schemaRegUsername:$schemaRegPassword",
            SCHEMA_REGISTRY_URL_CONFIG to schemaRegistryUrl
    )

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
