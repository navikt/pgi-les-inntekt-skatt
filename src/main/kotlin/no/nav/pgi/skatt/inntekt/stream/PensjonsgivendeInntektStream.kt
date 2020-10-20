package no.nav.pgi.skatt.inntekt.stream

import no.nav.samordning.pgi.schema.Hendelse
import no.nav.samordning.pgi.schema.HendelseKey
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.KStream
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.Properties


internal class PensjonsgivendeInntektStream(streamProperties: Properties) {

    private val log: Logger = LoggerFactory.getLogger(PensjonsgivendeInntektStream::class.java)
    private val pensjonsgivendeInntektStream = buildStreams(streamProperties)

    init {
        setStreamStateListener()
        setUncaughtStreamExceptionHandler()
    }

    private fun buildStreams(streamProperties: Properties): KafkaStreams {
        val streamBuilder = StreamsBuilder()
        val stream: KStream<HendelseKey, Hendelse> = streamBuilder.stream(PGI_HENDELSE_TOPIC)
        
        stream.mapValues(PensjonsgivendeInntektMapper())
                .to(PGI_INNTEKT_TOPIC)

        return KafkaStreams(streamBuilder.build(), streamProperties)
    }

    private fun setUncaughtStreamExceptionHandler() {
        pensjonsgivendeInntektStream.setUncaughtExceptionHandler { thread: Thread?, e: Throwable? ->
            log.error("Uncaught exception in thread $thread, closing beregnetSkattStream", e)
            pensjonsgivendeInntektStream.close()
        }
    }

    private fun setStreamStateListener() {
        pensjonsgivendeInntektStream.setStateListener { newState: KafkaStreams.State?, oldState: KafkaStreams.State? ->
            log.info("State change from $oldState to $newState")
        }
    }

    internal fun start() = pensjonsgivendeInntektStream.start()
    internal fun close() = pensjonsgivendeInntektStream.close()
}