package no.nav.pgi.skatt.inntekt.common

import no.nav.pgi.skatt.inntekt.stream.KafkaConfig
import no.nav.pgi.skatt.inntekt.stream.PGI_HENDELSE_TOPIC
import no.nav.pgi.skatt.inntekt.stream.PGI_INNTEKT_TOPIC
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.context.junit.jupiter.SpringExtension


@ExtendWith(SpringExtension::class)
@EmbeddedKafka(partitions = 1, topics = [PGI_INNTEKT_TOPIC, PGI_HENDELSE_TOPIC])
@SpringBootTest
internal class KafkaTestEnvironment {

    @Autowired
    lateinit var embeddedKafka: EmbeddedKafkaBroker

    private val inntektTestConsumer = inntektTestConsumer()
    private val hendelseTestProducer = hendelseTestProducer()

    init {
        inntektTestConsumer.subscribe(listOf(PGI_INNTEKT_TOPIC))
    }

    // TODO: bli kvitt denne?
    internal fun testConfiguration() = mapOf(
//        KafkaConfig.BOOTSTRAP_SERVERS to kafkaTestEnvironment.brokersURL,
//        KafkaConfig.SCHEMA_REGISTRY to schemaRegistryUrl,
        KafkaConfig.SCHEMA_REGISTRY_USERNAME to "mrOpenSource",
        KafkaConfig.SCHEMA_REGISTRY_PASSWORD to "opensourcedPassword"
    )


    private fun inntektTestConsumer(): KafkaConsumer<String, String> {
        return KafkaConsumer<String, String>(
            KafkaTestUtils.consumerProps("group", "false", embeddedKafka)
        )
    }

    private fun hendelseTestProducer(): KafkaProducer<String, String> {
        return KafkaProducer<String, String>(
            KafkaTestUtils.producerProps(embeddedKafka)
        )
    }
}