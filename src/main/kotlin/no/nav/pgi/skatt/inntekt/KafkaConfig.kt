package no.nav.pgi.skatt.inntekt

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.security.auth.SecurityProtocol.SASL_SSL
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.StreamsConfig.*
import java.util.*


internal const val APPLICATION_ID = "pgi-les-inntekt-skatt-12341234"

internal class KafkaConfig(environment: Map<String, String> = System.getenv()) {

    private val bootstrapServers = environment.getVal(BOOTSTRAP_SERVERS_ENV_KEY)
    private val schemaRegistryUrl = environment.getVal(SCHEMA_REGISTRY_URL_ENV_KEY)
    private val saslMechanism = environment.getVal(SASL_MECHANISM_ENV_KEY, "PLAIN")
    private val securityProtocol = environment.getVal(SECURITY_PROTOCOL_ENV_KEY, SASL_SSL.name)
    private val saslJaasConfig = createSaslJaasConfig(
            environment.getVal(USERNAME_ENV_KEY),
            environment.getVal(PASSWORD_ENV_KEY)
    )

    internal fun streamConfig(): Properties = Properties().apply {
        put(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        put("schema.registry.url", schemaRegistryUrl)
        put(SECURITY_PROTOCOL_CONFIG, securityProtocol)
        put(SaslConfigs.SASL_MECHANISM, saslMechanism)
        put(SaslConfigs.SASL_JAAS_CONFIG, saslJaasConfig)
        put(DEFAULT_KEY_SERDE_CLASS_CONFIG, SpecificAvroSerde::class.java)
        put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde::class.java)
        put(APPLICATION_ID_CONFIG, APPLICATION_ID)
        put(AUTO_OFFSET_RESET_CONFIG, "earliest")
    }

    private fun createSaslJaasConfig(username: String, password: String) =
            """org.apache.kafka.common.security.plain.PlainLoginModule required username="$username" password="$password";"""

    companion object {
        const val BOOTSTRAP_SERVERS_ENV_KEY = "KAFKA_BOOTSTRAP_SERVERS"
        const val SCHEMA_REGISTRY_URL_ENV_KEY = "KAFKA_SCHEMA_REGISTRY"
        const val USERNAME_ENV_KEY = "USERNAME"
        const val PASSWORD_ENV_KEY = "PASSWORD"
        const val SASL_MECHANISM_ENV_KEY = "KAFKA_SASL_MECHANISM"
        const val SECURITY_PROTOCOL_ENV_KEY = "KAFKA_SECURITY_PROTOCOL"
        const val PGI_HENDELSE_TOPIC = "privat-pgi-hendelse"
        const val PGI_INNTEKT_TOPIC = "privat-pgi-inntekt"
    }
}