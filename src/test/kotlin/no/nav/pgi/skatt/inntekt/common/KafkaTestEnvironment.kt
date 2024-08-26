package no.nav.pgi.skatt.inntekt.common

import no.nav.pgi.domain.Hendelse
import no.nav.pgi.domain.HendelseKey
import no.nav.pgi.domain.PensjonsgivendeInntekt
import no.nav.pgi.skatt.inntekt.stream.PGI_HENDELSE_TOPIC
import no.nav.pgi.skatt.inntekt.stream.PGI_INNTEKT_TOPIC
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig.*
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.Duration.ofSeconds


@ExtendWith(SpringExtension::class)
@EmbeddedKafka(partitions = 1, topics = [PGI_INNTEKT_TOPIC])
@SpringBootTest
internal class KafkaTestEnvironment {

    @Autowired
    lateinit var embeddedKafka: EmbeddedKafkaBroker

    private val inntektTestConsumer = inntektTestConsumer()
    private val hendelseTestProducer = hendelseTestProducer()

    init {
        inntektTestConsumer.subscribe(listOf(PGI_INNTEKT_TOPIC))
    }

    private fun inntektTestConsumer() = KafkaConsumer<HendelseKey, PensjonsgivendeInntekt>(
        mapOf(
//            CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to kafkaTestEnvironment.brokersURL,
            KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            GROUP_ID_CONFIG to "LOL",
            AUTO_OFFSET_RESET_CONFIG to "earliest",
            ENABLE_AUTO_COMMIT_CONFIG to false
        )
    )

    private fun hendelseTestProducer() = KafkaProducer<HendelseKey, Hendelse>(
        mapOf(
            //            CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to kafkaTestEnvironment.brokersURL,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.ACKS_CONFIG to "all",
            ProducerConfig.RETRIES_CONFIG to Integer.MAX_VALUE
        )
    )

    //Duration 4 seconds to allow for hendelse to be added to topic
    fun consumeInntektTopic(): List<ConsumerRecord<HendelseKey, PensjonsgivendeInntekt>> =
        inntektTestConsumer.poll(ofSeconds(4L)).records(PGI_INNTEKT_TOPIC).toList()

    internal fun writeHendelse(hendelseKey: HendelseKey, hendelse: Hendelse) {
        val record = ProducerRecord(PGI_HENDELSE_TOPIC, hendelseKey, hendelse)
        hendelseTestProducer.send(record).get()
    }

    fun pgiHendelseTopicOffsett() {
//        kafkaTestEnvironment
    }

    fun getFirstRecordOnInntektTopic() = consumeInntektTopic()[0]


    fun closeTestConsumer() = inntektTestConsumer.close()
}