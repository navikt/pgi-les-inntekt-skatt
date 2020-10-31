package no.nav.pgi.skatt.inntekt.stream

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import no.nav.pensjon.samhandling.env.getVal
import org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG
import org.apache.kafka.streams.StreamsConfig.*
import java.util.*

internal const val STREAM_APPLICATION_ID = "pgi-les-inntekt-skatt-12341234"
internal const val PGI_INNTEKT_TOPIC = "privat-pgi-inntekt"
internal const val PGI_HENDELSE_TOPIC = "privat-pgi-hendelse"

internal class KafkaConfig(environment: Map<String, String> = System.getenv(), private val securityStrategy: SecurityStrategy = SaslSslStrategy()) {
    private val bootstrapServers = environment.getVal(BOOTSTRAP_SERVERS_ENV_KEY)
    private val schemaRegistryUrl = environment.getVal(SCHEMA_REGISTRY_URL_ENV_KEY)

    internal fun streamConfig(): Properties = Properties().apply {
        put(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        put("schema.registry.url", schemaRegistryUrl)
        put(DEFAULT_KEY_SERDE_CLASS_CONFIG, SpecificAvroSerde::class.java)
        put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde::class.java)
        put(APPLICATION_ID_CONFIG, STREAM_APPLICATION_ID)
        put(AUTO_OFFSET_RESET_CONFIG, "earliest")
        putAll(securityStrategy.securityConfig())
    }

    companion object EnvironmentKeys {
        internal const val BOOTSTRAP_SERVERS_ENV_KEY = "ONPREM_KAFKA_BROKERS"
        internal const val SCHEMA_REGISTRY_URL_ENV_KEY = "ONPREM_SCHEMA_REGISTRY"
    }

    internal interface SecurityStrategy {
        fun securityConfig(): Map<String, String>
    }
}
