package no.nav.pgi.skatt.inntekt

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess
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
    private val scheduler = Executors.newSingleThreadScheduledExecutor()

    internal fun start() {
        addShutdownHook()
        scheduleShutdown()
        pgiStream.start()
    }

    private fun scheduleShutdown() {
        val delayInHours = 12L

        LOG.info("Scheduling graceful shutdown in $delayInHours hours to refresh Kafka consumer.")

        scheduler.schedule({
            LOG.info("Scheduled shutdown initiated. Exiting application to trigger pod restart.")
            exitProcess(0)
        }, delayInHours, TimeUnit.HOURS)
    }

    private fun addShutdownHook() {
        LOG.info("Adding shutdown hook")
        Runtime.getRuntime().addShutdownHook(Thread {
            try {
                LOG.info("Shutdown hook triggered closing pgiStream")
                pgiStream.close()
                scheduler.shutdown()
            } catch (e: Exception) {
                LOG.error("Error while shutting down", e)
            }
        })
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ApplicationService::class.java)
    }
}