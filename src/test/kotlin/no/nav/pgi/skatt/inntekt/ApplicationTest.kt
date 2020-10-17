package no.nav.pgi.skatt.inntekt

import no.nav.pgi.skatt.inntekt.kafka.KafkaConfig
import no.nav.samordning.pgi.schema.Hendelse
import no.nav.samordning.pgi.schema.HendelseKey
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import java.net.http.HttpResponse

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ApplicationTest {
    private val kafkaTestEnvironment = KafkaTestEnvironment()
    private val kafkaConfig = KafkaConfig(kafkaTestEnvironment.testConfiguration(), PlaintextStrategy())
    private val application = Application(kafkaConfig)
    private val skattInntektMock = SkattInntektMock()


    @BeforeAll
    fun init() {
        application.startPensjonsgivendeInntektStream()
        skattInntektMock.`stub inntekt fra skatt`()
    }

    @AfterAll
    fun tearDown() {
        application.stopPensjonsgivendeInntektStream()
        skattInntektMock.stop()
        kafkaTestEnvironment.tearDown()
        kafkaTestEnvironment.closeTestConsumer()
    }

    @Test
    fun `crude test of skatt mock`() {
        val client = SkattClient()
        val response = client.send(createGetRequest(SKATT_INNTEKT_URL), HttpResponse.BodyHandlers.ofString())
        println(response.body())
    }

    @Disabled
    @Test
    fun `crude test of kafka test environment`() {
        val hendelseKey = HendelseKey("1234", "2018")
        val hendelse = Hendelse(12345L, "1234", "2018")
        kafkaTestEnvironment.writeHendelse(hendelseKey, hendelse)
        assertEquals(hendelse, kafkaTestEnvironment.getFirstRecordOnInntektTopic())
    }
}