package no.nav.pgi.skatt.inntekt

import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.EnableScheduling
import kotlin.system.exitProcess

@Configuration
@EnableScheduling
@Profile("dev-gcp", "prod-gcp")
class Configuration {

    @Bean
    fun applicationService(
        meterRegistry: MeterRegistry,
    ) : ApplicationService {
        try {
            log.info("Configuration.applicationService: Starting ApplicationService")
            val application = ApplicationService()
            application.start()
            log.info("Configuration.applicationService: Started ApplicationService")
            return application
        } catch (e: Throwable) {
            exitProcess(1)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(ApplicationService::class.java)!!
    }


}