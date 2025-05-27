package no.nav.pgi.skatt.inntekt

import no.nav.pgi.skatt.inntekt.skatt.PgiClient
import no.nav.pgi.skatt.inntekt.stream.KafkaConfig
import no.nav.pgi.skatt.inntekt.stream.PGIStream
import no.nav.pgi.skatt.inntekt.stream.PGITopology
import org.slf4j.LoggerFactory

class ApplicationService(
    counters: Counters,
    kafkaConfig: KafkaConfig = KafkaConfig(),
    pgiClient: PgiClient = PgiClient()
) {
    private val pgiStream = PGIStream(
        streamProperties = kafkaConfig.streamProperties(),
        pgiTopology = PGITopology(
            counters = counters,
            pgiClient = pgiClient
        )
    )

    internal fun start() {
        addShutdownHook()
        pgiStream.start()
    }

    private fun addShutdownHook() {
        LOG.info("Adding shutdown hook")
        Runtime.getRuntime().addShutdownHook(Thread {
            try {
                LOG.info("Shutdown hook triggered closing pgiStream")
                pgiStream.close()
            } catch (e: Exception) {
                LOG.error("Error while shutting down", e)
            }
        })
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ApplicationService::class.java)
    }
}