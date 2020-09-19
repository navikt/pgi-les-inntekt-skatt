package no.nav.pgi.skatt.inntekt

import org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.security.auth.SecurityProtocol.SASL_SSL
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsConfig


internal const val APPLICATION_ID_CONFIG = "pgi-les-inntekt-skatt"

internal class KafkaConfig(environment: Map<String, String> = System.getenv()) {

    private val bootstrapServers = environment.getVal(BOOTSTRAP_SERVERS_ENV_KEY)
    private val saslMechanism = environment.getVal(SASL_MECHANISM_ENV_KEY, "PLAIN")
    private val securityProtocol = environment.getVal(SECURITY_PROTOCOL_ENV_KEY, SASL_SSL.name)
    private val saslJaasConfig = createSaslJaasConfig(
            environment.getVal(USERNAME_ENV_KEY),
            environment.getVal(PASSWORD_ENV_KEY)
    )

    internal fun streamConfig() = commonConfig() + mapOf(
            StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG to Serdes.String().javaClass.name,
            StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG to Serdes.String().javaClass.name,
            StreamsConfig.APPLICATION_ID_CONFIG to APPLICATION_ID_CONFIG,
            AUTO_OFFSET_RESET_CONFIG to "earliest"
    )

    private fun commonConfig() = mapOf(
            BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            SECURITY_PROTOCOL_CONFIG to securityProtocol,
            SaslConfigs.SASL_MECHANISM to saslMechanism,
            SaslConfigs.SASL_JAAS_CONFIG to saslJaasConfig
    )

    private fun createSaslJaasConfig(username: String, password: String) =
            """org.apache.kafka.common.security.plain.PlainLoginModule required username="$username" password="$password";"""

    companion object {
        const val BOOTSTRAP_SERVERS_ENV_KEY = "KAFKA_BOOTSTRAP_SERVERS"
        const val USERNAME_ENV_KEY = "USERNAME"
        const val PASSWORD_ENV_KEY = "PASSWORD"
        const val SASL_MECHANISM_ENV_KEY = "KAFKA_SASL_MECHANISM"
        const val SECURITY_PROTOCOL_ENV_KEY = "KAFKA_SECURITY_PROTOCOL"
        const val PGI_HENDELSE_TOPIC = "privat-pgi-hendelse"
        const val PGI_INNTEKT_TOPIC = "privat-pgi-inntekt"
    }
}