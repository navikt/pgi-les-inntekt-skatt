package no.nav.pgi.skatt.inntekt

import no.nav.common.KafkaEnvironment
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig.*
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.security.auth.SecurityProtocol
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import java.time.Duration.ofSeconds

const val KAFKA_TEST_USERNAME = "srvTest"
const val KAFKA_TEST_PASSWORD = "opensourcedPassword"

class KafkaTestEnvironment {

    private val kafkaTestEnvironment: KafkaEnvironment = KafkaEnvironment(
            topicNames = listOf(
                    KafkaConfig.PGI_HENDELSE_TOPIC,
                    KafkaConfig.PGI_INNTEKT_TOPIC
            )
    )

    private val inntektTestConsumer = inntektTestConsumer()
    private val hendelseTestProducer = hendelseTestProducer()

    init {
        kafkaTestEnvironment.start()
        inntektTestConsumer.subscribe(listOf(KafkaConfig.PGI_INNTEKT_TOPIC))
    }

    internal fun tearDown() = kafkaTestEnvironment.tearDown()

    internal fun testConfiguration() = mapOf<String, String>(
            KafkaConfig.BOOTSTRAP_SERVERS_ENV_KEY to kafkaTestEnvironment.brokersURL,
            KafkaConfig.USERNAME_ENV_KEY to KAFKA_TEST_USERNAME,
            KafkaConfig.PASSWORD_ENV_KEY to KAFKA_TEST_PASSWORD,
            KafkaConfig.SECURITY_PROTOCOL_ENV_KEY to SecurityProtocol.PLAINTEXT.name
    )

    private fun inntektTestConsumer() = KafkaConsumer<String, String>(
            mapOf(
                    CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to kafkaTestEnvironment.brokersURL,
                    KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                    VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                    GROUP_ID_CONFIG to "LOL",
                    AUTO_OFFSET_RESET_CONFIG to "earliest",
                    ENABLE_AUTO_COMMIT_CONFIG to false
            )
    )

    private fun hendelseTestProducer() = KafkaProducer<String, String>(
            mapOf(
                    CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to kafkaTestEnvironment.brokersURL,
                    ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                    ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                    ProducerConfig.ACKS_CONFIG to "all",
                    ProducerConfig.RETRIES_CONFIG to Integer.MAX_VALUE
            )
    )

    //Duration 4 seconds to allow for hendelse to be added to topic
    fun consumeInntektTopic(): List<ConsumerRecord<String, String>> = inntektTestConsumer.poll(ofSeconds(4)).records(KafkaConfig.PGI_HENDELSE_TOPIC).toList()

    internal fun writeHendelse(hendelseKey: String, hendelse: String) {
        val record = ProducerRecord(KafkaConfig.PGI_HENDELSE_TOPIC, hendelseKey, hendelse)
        hendelseTestProducer.send(record).get()
    }

    fun getFirstRecordOnInntektTopic() = consumeInntektTopic()[0]
}