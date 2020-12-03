package no.nav.pgi.skatt.inntekt

import no.nav.pgi.skatt.inntekt.mock.MaskinportenMock
import no.nav.pgi.skatt.inntekt.mock.MaskinportenMock.Companion.MASKINPORTEN_ENV_VARIABLES
import no.nav.pgi.skatt.inntekt.mock.PENSJONGIVENDE_INNTEKT_MOCK_HOST
import no.nav.pgi.skatt.inntekt.mock.PensjonsgivendeInntektMock
import no.nav.pgi.skatt.inntekt.stream.KafkaConfig
import no.nav.samordning.pgi.schema.Hendelse
import no.nav.samordning.pgi.schema.HendelseKey
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals

private const val INNTEKTSAAR = "2020"
private const val NORSK_PERSONIDENTIFIKATOR = "12345678901"

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ComponentTest {
    private val kafkaTestEnvironment = KafkaTestEnvironment()
    private val kafkaConfig = KafkaConfig(kafkaTestEnvironment.testConfiguration(), PlaintextStrategy())
    private val pensjonsgivendeInntektMock = PensjonsgivendeInntektMock()
    private val maskinportenMock = MaskinportenMock()
    private val skattClient = PgiClient(mapOf(PENSJONGIVENDE_INNTEKT_HOST_ENV_KEY to PENSJONGIVENDE_INNTEKT_MOCK_HOST) + MASKINPORTEN_ENV_VARIABLES)
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
        application.stop()
        pensjonsgivendeInntektMock.stop()
        maskinportenMock.stop()
        kafkaTestEnvironment.tearDown()
        kafkaTestEnvironment.closeTestConsumer()
    }

    @Test
    fun `reads hendelser from topic, gets pgi based on hendelse, produces inntekt to topic`() {
        pensjonsgivendeInntektMock.`stub pensjongivende inntekt`(INNTEKTSAAR, NORSK_PERSONIDENTIFIKATOR)

        val hendelseKey = HendelseKey(NORSK_PERSONIDENTIFIKATOR, INNTEKTSAAR)
        val hendelse = Hendelse(12345L, NORSK_PERSONIDENTIFIKATOR, INNTEKTSAAR)

        kafkaTestEnvironment.writeHendelse(hendelseKey, hendelse)
        assertEquals(hendelseKey, kafkaTestEnvironment.getFirstRecordOnInntektTopic().key())
    }

    /*
    @Test
    fun `reads hendelser from topic, gets 401 from skatt`() {
        val hendelseKey = HendelseKey(NORSK_PERSONIDENTIFIKATOR, INNTEKTSAAR)
        val hendelse = Hendelse(12345L, NORSK_PERSONIDENTIFIKATOR, INNTEKTSAAR)

        pensjonsgivendeInntektMock.`stub 401 fra skatt`(INNTEKTSAAR,NORSK_PERSONIDENTIFIKATOR)
        kafkaTestEnvironment.writeHendelse(hendelseKey, hendelse)
    }

     */

}