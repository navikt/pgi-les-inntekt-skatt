package no.nav.pgi.skatt.inntekt.common

import no.nav.pgi.skatt.inntekt.stream.KafkaConfig
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.common.security.auth.SecurityProtocol

internal class PlaintextStrategy : KafkaConfig.SecurityStrategy {
    override fun securityConfig() = mapOf(
        CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to SecurityProtocol.PLAINTEXT.name
    )
}