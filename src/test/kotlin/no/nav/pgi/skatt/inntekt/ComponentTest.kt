package no.nav.pgi.skatt.inntekt

import no.nav.pgi.domain.Hendelse
import no.nav.pgi.domain.HendelseKey
import org.junit.jupiter.api.TestInstance

private const val INNTEKTSAAR = "2020"
private const val NORSK_PERSONIDENTIFIKATOR = "12345678901"

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ComponentTest {
    /*
    private val kafkaTestEnvironment = KafkaTestEnvironment()
    private val kafkaConfig = KafkaConfig(kafkaTestEnvironment.testConfiguration(), PlaintextStrategy())
    private val pensjonsgivendeInntektMock = PensjonsgivendeInntektMock()
    private val maskinportenMock = MaskinportenMock()
    private val pgiClient = PgiClient(PensjonsgivendeInntektMock.PGI_CLIENT_ENV_VARIABLES + MASKINPORTEN_CLIENT_ENV_VARIABLES)
    private val application = Application(kafkaConfig, pgiClient)

    @BeforeAll
    fun init() {
        application.start()
        maskinportenMock.`stub maskinporten token endpoint`()
    }

    @AfterAll
    fun tearDown() {
        application.stop()
        kafkaTestEnvironment.closeTestConsumer()
        kafkaTestEnvironment.tearDown()
        pensjonsgivendeInntektMock.stop()
        maskinportenMock.stop()
    }

    @Test
    fun `reads hendelser from topic, gets pgi based on hendelse, produces inntekt to topic`() {
        val discardedHendelse = Hendelse(12346L, "11111111111", INNTEKTSAAR, HendelseMetadata(0))
        val hendelse = Hendelse(12345L, NORSK_PERSONIDENTIFIKATOR, INNTEKTSAAR, HendelseMetadata(0))

        pensjonsgivendeInntektMock.`stub pensjongivende inntekt`(INNTEKTSAAR, NORSK_PERSONIDENTIFIKATOR)
        pensjonsgivendeInntektMock.`stub error code from skatt`(discardedHendelse, ErrorCodesSkatt.skattDiscardErrorCodes.first())

        kafkaTestEnvironment.writeHendelse(discardedHendelse.key(), discardedHendelse)
        kafkaTestEnvironment.writeHendelse(hendelse.key(), hendelse)

        assertEquals(hendelse.key(), kafkaTestEnvironment.getFirstRecordOnInntektTopic().key())
    }

     */
}

private fun Hendelse.key() = HendelseKey(identifikator, gjelderPeriode)