package no.nav.pgi.skatt.inntekt

import no.nav.pgi.skatt.inntekt.stream.KafkaConfig
import no.nav.samordning.pgi.schema.Hendelse
import no.nav.samordning.pgi.schema.HendelseKey
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import java.net.http.HttpResponse

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ComponentTest {
    private val kafkaTestEnvironment = KafkaTestEnvironment()
    private val kafkaConfig = KafkaConfig(kafkaTestEnvironment.testConfiguration(), PlaintextStrategy())
    private val skattInntektMock = SkattInntektMock()
    private val application = Application(kafkaConfig)

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

    @Disabled("Under construction")
    @Test
    fun `crude test of skatt mock`() {
        val client = SkattClient()
        val response = client.send(createGetRequest(SKATT_INNTEKT_URL), HttpResponse.BodyHandlers.ofString())
        println(response.body())
    }

    @Test
    fun `reads hendelser from topic, gets pgi based on hendelse, produces inntekt to topic`() {
        val hendelseKey = HendelseKey("12345678901", "2020")
        val hendelse = Hendelse(12345L, "12345678901", "2020")

        kafkaTestEnvironment.writeHendelse(hendelseKey, hendelse)
        assertEquals(hendelseKey, kafkaTestEnvironment.getFirstRecordOnInntektTopic().key())
    }
}