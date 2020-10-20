package no.nav.pgi.skatt.inntekt.stream

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import no.nav.samordning.pgi.schema.Hendelse
import no.nav.samordning.pgi.schema.HendelseKey
import no.nav.samordning.pgi.schema.PensjonsgivendeInntekt
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.kstream.ValueMapperWithKey
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*


internal class PensjonsgivendeInntektStream(streamProperties: Properties) {

    private val log: Logger = LoggerFactory.getLogger(PensjonsgivendeInntektStream::class.java)
    private val streamBuilder = StreamsBuilder()
    private val pensjonsgivendeInntektStream = buildStreams(streamProperties)
    private val hendelseKeySerde: SpecificAvroSerde<HendelseKey> = SpecificAvroSerde()
    private val hendelseSerde: SpecificAvroSerde<Hendelse> = SpecificAvroSerde()
    private val pensjonsgivendeInntektSerde: SpecificAvroSerde<PensjonsgivendeInntekt> = SpecificAvroSerde()

    init {
        val serdeConfig = mapOf("schema.registry.url" to streamProperties["schema.registry.url"])
        hendelseKeySerde.configure(serdeConfig, true)
        hendelseSerde.configure(serdeConfig, false)
        pensjonsgivendeInntektSerde.configure(serdeConfig, false)
        setStreamStateListener()
        setUncaughtStreamExceptionHandler()
    }

    private fun buildStreams(streamProperties: Properties): KafkaStreams {
        val stream: KStream<HendelseKey, Hendelse> = streamBuilder.stream(PGI_HENDELSE_TOPIC, Consumed.with(hendelseKeySerde, hendelseSerde))
        stream.mapValues(PensjonsgivendeInntektMapper())
                .to(PGI_INNTEKT_TOPIC, Produced.with(hendelseKeySerde, pensjonsgivendeInntektSerde))

        return KafkaStreams(streamBuilder.build(), streamProperties)
    }

    private fun setUncaughtStreamExceptionHandler() {
        pensjonsgivendeInntektStream.setUncaughtExceptionHandler { thread: Thread?, e: Throwable? ->
            log.debug("Uncaught exception in thread $thread, closing beregnetSkattStream", e)
            pensjonsgivendeInntektStream.close()
        }
    }

    private fun setStreamStateListener() {
        pensjonsgivendeInntektStream.setStateListener { newState: KafkaStreams.State?, oldState: KafkaStreams.State? ->
            log.debug("State change from $oldState to $newState")
        }
    }

    internal fun start() = pensjonsgivendeInntektStream.start()
    internal fun close() = pensjonsgivendeInntektStream.close()
}

class PensjonsgivendeInntektMapper : ValueMapperWithKey<HendelseKey, Hendelse, PensjonsgivendeInntekt> {
    override fun apply(key: HendelseKey, value: Hendelse): PensjonsgivendeInntekt {
        return PensjonsgivendeInntekt("12345678901", "2020")
    }
}