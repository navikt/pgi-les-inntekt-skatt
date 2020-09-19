package no.nav.pgi.skatt.inntekt

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.net.http.HttpResponse

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ApplicationTest {
    private val application = createApplication()
    private val kafkaTestEnvironment = KafkaTestEnvironment()
    private val kafkaConfig = KafkaConfig(kafkaTestEnvironment.testConfiguration())
    private val skattInntektMock = SkattInntektMock()


    @BeforeAll
    fun init() {
        application.start()
        skattInntektMock.`stub inntekt fra skatt`()
    }

    @AfterAll
    fun tearDown() {
        application.stop(100, 100)
        skattInntektMock.stop()
        kafkaTestEnvironment.tearDown()
    }

    @Test
    fun `crude test of skatt mock`() {
        val client = SkattClient()
        val response = client.send(createGetRequest(SKATT_INNTEKT_URL), HttpResponse.BodyHandlers.ofString())
        println(response.body())
    }

    @Test
    fun `crude test of kafka test environment`() {
        kafkaTestEnvironment.writeHendelse("hendelseKey", "hendelse")
    }
}