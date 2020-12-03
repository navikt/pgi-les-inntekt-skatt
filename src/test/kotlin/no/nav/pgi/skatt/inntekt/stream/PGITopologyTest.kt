package no.nav.pgi.skatt.inntekt.stream

import no.nav.pgi.skatt.inntekt.PENSJONGIVENDE_INNTEKT_HOST_ENV_KEY
import no.nav.pgi.skatt.inntekt.PgiClient
import no.nav.pgi.skatt.inntekt.PlaintextStrategy
import no.nav.pgi.skatt.inntekt.mock.MaskinportenMock
import no.nav.pgi.skatt.inntekt.mock.PensjonsgivendeInntektMock
import no.nav.pgi.skatt.inntekt.mock.PgiTopologyTestDriver
import no.nav.pgi.skatt.inntekt.mock.PgiTopologyTestDriver.Companion.MOCK_SCHEMA_REGISTRY_URL
import no.nav.pgi.skatt.inntekt.stream.mapping.PensjonsgivendeInntektClientException
import no.nav.samordning.pgi.schema.Hendelse
import no.nav.samordning.pgi.schema.HendelseKey
import no.nav.samordning.pgi.schema.PensjonsgivendeInntekt
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals

private const val ONE_HUNDRED = 100
private const val TEN = 10

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class PGITopologyTest {
    private val pensjonsgivendeInntektMock = PensjonsgivendeInntektMock()
    private val maskinportenMock = MaskinportenMock()

    private val kafkaConfig = KafkaConfig(getKafkaTestEnv(), PlaintextStrategy())
    private val topologyDriver = PgiTopologyTestDriver(PGITopology(PgiClient(getPgiClientEnv())).topology(), kafkaConfig.streamProperties())
    val testInputTopic = topologyDriver.createInputTopic<HendelseKey, Hendelse>(PGI_HENDELSE_TOPIC, MOCK_SCHEMA_REGISTRY_URL)
    val testOutputTopic = topologyDriver.createOutputTopic<HendelseKey, PensjonsgivendeInntekt>(PGI_INNTEKT_TOPIC, MOCK_SCHEMA_REGISTRY_URL)

    @BeforeAll
    fun init() {
        maskinportenMock.`stub maskinporten token endpoint`()
    }

    @AfterEach
    fun afterEach() {
        pensjonsgivendeInntektMock.reset()
    }

    @AfterAll
    fun tearDown() {
        maskinportenMock.stop()
        pensjonsgivendeInntektMock.stop()
    }

    @Test
    internal fun `should add 100 PensjonsgivendeInntekt to pgi-inntekt topic when 100 hendelser is added to pgi-hendelse topic`() {
        pensjonsgivendeInntektMock.`stub pensjongivende inntekt endpoint`()

        addToHendelseTopic(ONE_HUNDRED)

        val output = testOutputTopic.readKeyValuesToList()

        assertEquals(ONE_HUNDRED, pensjonsgivendeInntektMock.callsToMock())
        assertEquals(ONE_HUNDRED, output.size)
    }

    @Test
    internal fun `should fail with PensjonsgivendeInntektClientException if unhandled statuscode is returned from skatt`() {
        val innteksAar = "2019"
        val identifikator = "12345678901"
        val failingHendelse = Hendelse(1L, identifikator, innteksAar)

        pensjonsgivendeInntektMock.`stub pensjongivende inntekt endpoint`()
        pensjonsgivendeInntektMock.`stub 401 fra skatt`(innteksAar, identifikator)

        addToHendelseTopic(TEN)

        assertThrows<PensjonsgivendeInntektClientException> { addToTopic(failingHendelse) }
        assertEquals(TEN, testOutputTopic.readKeyValuesToList().size)
    }

    private fun getPgiClientEnv() =
            MaskinportenMock.MASKINPORTEN_ENV_VARIABLES + mapOf(PENSJONGIVENDE_INNTEKT_HOST_ENV_KEY to PensjonsgivendeInntektMock.HOST)

    private fun getKafkaTestEnv() =
            mapOf(KafkaConfig.BOOTSTRAP_SERVERS to "test",
                    KafkaConfig.SCHEMA_REGISTRY_USERNAME to "test",
                    KafkaConfig.SCHEMA_REGISTRY_PASSWORD to "test",
                    KafkaConfig.SCHEMA_REGISTRY to MOCK_SCHEMA_REGISTRY_URL)

    private fun addToHendelseTopic(amount: Int) = createHendelseList(amount).forEach { addToTopic(it) }
    private fun addToTopic(hendelse: Hendelse) = testInputTopic.pipeInput(hendelse.key(), hendelse)
    private fun createHendelseList(count: Int) = (1..count).map { Hendelse(it.toLong(), (10000000000 + it).toString(), "2018") }
}

private fun Hendelse.key() = HendelseKey(getIdentifikator(), getGjelderPeriode())


//TODO test hvor noen meldinger går først så exception for deretter se om ting går igjennom

//TODO verifisering av 1000 kall mot skatt mock





