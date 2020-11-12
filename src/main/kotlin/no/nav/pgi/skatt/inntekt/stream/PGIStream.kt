package no.nav.pgi.skatt.inntekt.stream

import no.nav.pensjon.samhandling.maskfnr.maskFnr
import no.nav.pgi.skatt.inntekt.PgiClient
import no.nav.samordning.pgi.schema.Hendelse
import no.nav.samordning.pgi.schema.HendelseKey
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.KStream
import org.slf4j.LoggerFactory
import java.util.*


internal class PGIStream(streamProperties: Properties, pgiClient: PgiClient = PgiClient()) {

    private val pensjonsgivendeInntektStream = KafkaStreams(setupStreamTopology(pgiClient), streamProperties)

    init {
        setStreamStateListener()
        setUncaughtStreamExceptionHandler()
    }

    private fun setupStreamTopology(pgiClient: PgiClient): Topology {
        val streamBuilder = StreamsBuilder()
        val stream: KStream<HendelseKey, Hendelse> = streamBuilder.stream(PGI_HENDELSE_TOPIC)

        stream.peek(logHendelseAboutToBeProcessed())
                .mapValues(FetchPgiFromSkatt(pgiClient))
                .mapValues(MapToPgiDto())
                .mapValues(MapToPgiAvro())
                .to(PGI_INNTEKT_TOPIC)

        return streamBuilder.build()
    }

    private fun logHendelseAboutToBeProcessed(): (HendelseKey, Hendelse) -> Unit =
            { _: HendelseKey, hendelse: Hendelse -> LOG.info("Started processing hendelse ${hendelse.toString().maskFnr()}") }

    private fun setUncaughtStreamExceptionHandler() {
        pensjonsgivendeInntektStream.setUncaughtExceptionHandler { thread: Thread?, e: Throwable? ->
            LOG.error("Uncaught exception in thread $thread, closing beregnetSkattStream", e)
            pensjonsgivendeInntektStream.close()
        }
    }

    private fun setStreamStateListener() {
        pensjonsgivendeInntektStream.setStateListener { newState: KafkaStreams.State?, oldState: KafkaStreams.State? ->
            LOG.info("State change from $oldState to $newState")
        }
    }

    internal fun start() = pensjonsgivendeInntektStream.start()
    internal fun close() = pensjonsgivendeInntektStream.close()

    companion object {
        private val LOG = LoggerFactory.getLogger(PGIStream::class.java)
    }
}
