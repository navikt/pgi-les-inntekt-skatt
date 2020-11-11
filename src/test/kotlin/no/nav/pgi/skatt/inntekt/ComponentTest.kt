package no.nav.pgi.skatt.inntekt

import no.nav.pgi.skatt.inntekt.mock.MaskinportenMock
import no.nav.pgi.skatt.inntekt.mock.MaskinportenMock.Companion.MASKINPORTEN_ENV_VARIABLES
import no.nav.pgi.skatt.inntekt.mock.PENSJONGIVENDE_INNTEKT_MOCK_URL
import no.nav.pgi.skatt.inntekt.mock.PensjonsgivendeInntektMock
import no.nav.pgi.skatt.inntekt.stream.KafkaConfig
import no.nav.samordning.pgi.schema.Hendelse
import no.nav.samordning.pgi.schema.HendelseKey
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ComponentTest {
    private val kafkaTestEnvironment = KafkaTestEnvironment()
    private val kafkaConfig = KafkaConfig(kafkaTestEnvironment.testConfiguration(), PlaintextStrategy())
    private val pensjonsgivendeInntektMock = PensjonsgivendeInntektMock()
    private val maskinportenMock = MaskinportenMock()
    private val skattClient = PensjonsgivendeInntektClient(mapOf(PENSJONGIVENDE_INNTEKT_URL_ENV_KEY to PENSJONGIVENDE_INNTEKT_MOCK_URL) + MASKINPORTEN_ENV_VARIABLES)
    private val application = Application(kafkaConfig, skattClient)

    @BeforeAll
    fun init() {
        application.startPensjonsgivendeInntektStream()
        maskinportenMock.`stub maskinporten token endpoint`()
    }

    @BeforeEach
    fun beforeEach() {
        pensjonsgivendeInntektMock.reset()
    }

    @AfterAll
    fun tearDown() {
        application.stopPensjonsgivendeInntektStream()
        pensjonsgivendeInntektMock.stop()
        maskinportenMock.stop()
        kafkaTestEnvironment.tearDown()
        kafkaTestEnvironment.closeTestConsumer()
    }

    @Test
    fun `reads hendelser from topic, gets pgi based on hendelse, produces inntekt to topic`() {
        val inntektsaar = "2020"
        val norskPersonidentifikator = "12345678901"
        pensjonsgivendeInntektMock.`stub pensjongivende inntekt`(inntektsaar, norskPersonidentifikator)

        val hendelseKey = HendelseKey(norskPersonidentifikator, inntektsaar)
        val hendelse = Hendelse(12345L, norskPersonidentifikator, inntektsaar)

        kafkaTestEnvironment.writeHendelse(hendelseKey, hendelse)
        assertEquals(hendelseKey, kafkaTestEnvironment.getFirstRecordOnInntektTopic().key())
    }

    @Test
    fun `reads hendelser from topic, gets 401 from skatt`() {
        val hendelseKey = HendelseKey("12345678901", "2020")
        val hendelse = Hendelse(12345L, "12345678901", "2020")

        pensjonsgivendeInntektMock.`stub 401 fra skatt`()
        kafkaTestEnvironment.writeHendelse(hendelseKey, hendelse)
    }


}