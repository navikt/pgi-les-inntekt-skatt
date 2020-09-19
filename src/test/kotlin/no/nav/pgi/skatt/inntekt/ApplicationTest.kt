package no.nav.pgi.skatt.inntekt

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ApplicationTest {
    private val application = createApplication()
    private val kafkaTestEnvironment = KafkaTestEnvironment()
    private val kafkaConfig = KafkaConfig(kafkaTestEnvironment.testConfiguration())


    @BeforeAll
    fun init() {
        application.start()
    }

    @AfterAll
    fun tearDown() {
        application.stop(100, 100)
        kafkaTestEnvironment.tearDown()
    }

    @Test
    fun `crude test of kafka test environment`() {
        kafkaTestEnvironment.writeHendelse("hendelseKey", "hendelse")
    }
}