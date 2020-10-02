package no.nav.pgi.skatt.inntekt

import no.nav.samordning.pgi.schema.Hendelse
import no.nav.samordning.pgi.schema.HendelseKey
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Printed
import org.apache.kafka.streams.kstream.Produced
import java.util.*


internal class PensjonsgivendeInntektStream(streamProperties: Properties) {

    private val streamBuilder = StreamsBuilder()
    private val pensjonsgivendeInntektStream = buildStreams(streamProperties)

    init {
        setStreamStateListener()
        setUncaughtStreamExceptionHandler()
    }

    private fun buildStreams(streamProperties: Properties): KafkaStreams {
        val stream: KStream<HendelseKey, Hendelse> = streamBuilder.stream(KafkaConfig.PGI_HENDELSE_TOPIC)
        stream.filter { key, value -> true }.to(KafkaConfig.PGI_INNTEKT_TOPIC)
        return KafkaStreams(streamBuilder.build(), streamProperties)
    }

    private fun setUncaughtStreamExceptionHandler() {
        pensjonsgivendeInntektStream.setUncaughtExceptionHandler { thread: Thread?, e: Throwable? ->
            println("Uncaught exception in thread $thread, closing beregnetSkattStream. StackTrace: ${e?.stackTrace}")
            pensjonsgivendeInntektStream.close()
        }
    }

    private fun setStreamStateListener() {
        pensjonsgivendeInntektStream.setStateListener { newState: KafkaStreams.State?, oldState: KafkaStreams.State?
            -> println("State change from $oldState to $newState") }
    }

    internal fun start() = pensjonsgivendeInntektStream.start()
    internal fun close() = pensjonsgivendeInntektStream.close()

}