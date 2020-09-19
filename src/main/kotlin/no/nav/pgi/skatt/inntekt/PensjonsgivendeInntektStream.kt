package no.nav.pgi.skatt.inntekt

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Printed
import org.apache.kafka.streams.kstream.Produced
import java.util.*


internal class PensjonsgivendeInntektStream {

    private val streamBuilder = StreamsBuilder()

    internal fun buildStreams(streamProperties: Properties): KafkaStreams {
        val stream: KStream<String, String> = streamBuilder.stream(KafkaConfig.PGI_HENDELSE_TOPIC)
        stream.to(KafkaConfig.PGI_INNTEKT_TOPIC)
        return KafkaStreams(streamBuilder.build(), streamProperties)
    }
}