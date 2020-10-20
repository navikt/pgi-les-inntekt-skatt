package no.nav.pgi.skatt.inntekt

import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import io.confluent.kafka.serializers.KafkaAvroSerializer
import no.nav.common.KafkaEnvironment
import no.nav.pgi.skatt.inntekt.stream.KafkaConfig
import no.nav.pgi.skatt.inntekt.stream.PGI_HENDELSE_TOPIC
import no.nav.pgi.skatt.inntekt.stream.PGI_INNTEKT_TOPIC
import no.nav.samordning.pgi.schema.Hendelse
import no.nav.samordning.pgi.schema.HendelseKey
import no.nav.samordning.pgi.schema.PensjonsgivendeInntekt
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig.*
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import java.time.Duration.ofSeconds


class KafkaTestEnvironment {

    private val kafkaTestEnvironment: KafkaEnvironment = KafkaEnvironment(
            withSchemaRegistry = true,
            topicNames = listOf(PGI_HENDELSE_TOPIC, PGI_INNTEKT_TOPIC)
    )

    private val inntektTestConsumer = inntektTestConsumer()
    private val hendelseTestProducer = hendelseTestProducer()

    init {
        kafkaTestEnvironment.start()
        inntektTestConsumer.subscribe(listOf(PGI_INNTEKT_TOPIC))
    }

    private val schemaRegistryUrl: String
        get() = kafkaTestEnvironment.schemaRegistry!!.url

    internal fun tearDown() = kafkaTestEnvironment.tearDown()

    internal fun testConfiguration() = mapOf<String, String>(
            KafkaConfig.BOOTSTRAP_SERVERS_ENV_KEY to kafkaTestEnvironment.brokersURL,
            KafkaConfig.SCHEMA_REGISTRY_URL_ENV_KEY to schemaRegistryUrl,
    )

    private fun inntektTestConsumer() = KafkaConsumer<HendelseKey, PensjonsgivendeInntekt>(
            mapOf(
                    CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to kafkaTestEnvironment.brokersURL,
                    "schema.registry.url" to schemaRegistryUrl,
                    KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG to true,
                    KEY_DESERIALIZER_CLASS_CONFIG to KafkaAvroDeserializer::class.java,
                    VALUE_DESERIALIZER_CLASS_CONFIG to KafkaAvroDeserializer::class.java,
                    GROUP_ID_CONFIG to "LOL",
                    AUTO_OFFSET_RESET_CONFIG to "earliest",
                    ENABLE_AUTO_COMMIT_CONFIG to false
            )
    )

    private fun hendelseTestProducer() = KafkaProducer<HendelseKey, Hendelse>(
            mapOf(
                    CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to kafkaTestEnvironment.brokersURL,
                    "schema.registry.url" to schemaRegistryUrl,
                    ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to KafkaAvroSerializer::class.java,
                    ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to KafkaAvroSerializer::class.java,
                    ProducerConfig.ACKS_CONFIG to "all",
                    ProducerConfig.RETRIES_CONFIG to Integer.MAX_VALUE
            )
    )

    //Duration 4 seconds to allow for hendelse to be added to topic
    private fun consumeInntektTopic(): List<ConsumerRecord<HendelseKey, PensjonsgivendeInntekt>> = inntektTestConsumer.poll(ofSeconds(4L)).records(PGI_INNTEKT_TOPIC).toList()

    internal fun writeHendelse(hendelseKey: HendelseKey, hendelse: Hendelse) {
        val record = ProducerRecord(PGI_HENDELSE_TOPIC, hendelseKey, hendelse)
        hendelseTestProducer.send(record).get()
    }

    fun getFirstRecordOnInntektTopic() = consumeInntektTopic()[0]

    fun closeTestConsumer() = inntektTestConsumer.close()
}