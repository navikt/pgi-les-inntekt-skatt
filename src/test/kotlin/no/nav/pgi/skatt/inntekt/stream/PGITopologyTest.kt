package no.nav.pgi.skatt.inntekt.stream

import no.nav.pgi.domain.Hendelse
import no.nav.pgi.domain.HendelseKey
import no.nav.pgi.domain.HendelseMetadata
import no.nav.pgi.domain.serialization.PgiDomainSerializer
import no.nav.pgi.skatt.inntekt.common.PlaintextStrategy
import no.nav.pgi.skatt.inntekt.mock.MaskinportenMock
import no.nav.pgi.skatt.inntekt.mock.PensjonsgivendeInntektMock
import no.nav.pgi.skatt.inntekt.mock.PgiTopologyTestDriver
import no.nav.pgi.skatt.inntekt.mock.PgiTopologyTestDriver.Companion.MOCK_SCHEMA_REGISTRY_URL
import no.nav.pgi.skatt.inntekt.skatt.PgiClient
import no.nav.pgi.skatt.inntekt.skatt.RateLimit
import no.nav.pgi.skatt.inntekt.stream.mapping.FeilmedlingFraSkattException
import org.apache.kafka.streams.errors.StreamsException
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import kotlin.time.Duration.Companion.seconds

private const val ONE_HUNDRED = 100
private const val TEN = 10

private const val INNTEKTSAAR = "2019"
private const val IDENTIFIKATOR = "12345678901"

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class PGITopologyTest {
    private val pensjonsgivendeInntektMock = PensjonsgivendeInntektMock()
    private val maskinportenMock = MaskinportenMock()

    private val pgiClient = PgiClient(
        env = PensjonsgivendeInntektMock.PGI_CLIENT_ENV_VARIABLES + MaskinportenMock.MASKINPORTEN_CLIENT_ENV_VARIABLES,
        rateLimit = RateLimit(rate = 1000, timeInterval = 1.seconds)
    )
    private val kafkaConfig = KafkaConfig(getKafkaTestEnv(), PlaintextStrategy())
    private val topologyDriver =
        PgiTopologyTestDriver(PGITopology(pgiClient).topology(), kafkaConfig.streamProperties())

    val testInputTopic =
        topologyDriver.createInputTopic(PGI_HENDELSE_TOPIC)
    val testOutputTopic = topologyDriver.createOutputTopic(
        PGI_INNTEKT_TOPIC
    )

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
        topologyDriver.close()
    }

    @Test
    internal fun `should add 100 PensjonsgivendeInntekt to pgi-inntekt topic when 100 hendelser is added to pgi-hendelse topic`() {
        pensjonsgivendeInntektMock.`stub pensjongivende inntekt endpoint`()

        addToHendelseTopic(ONE_HUNDRED)

        val output = testOutputTopic.readKeyValuesToList()

        assertEquals(ONE_HUNDRED, pensjonsgivendeInntektMock.callsToMock())
        assertEquals(ONE_HUNDRED, output.size)
    }

//    @Test //TODO vurder å legg denne tilbake etterhvert som vi får oversikt over eventuelle feil vi kan/bør hoppe over/discarde
//    internal fun `should not process skattDiscardErrorCodes`() {
//        pensjonsgivendeInntektMock.`stub pensjongivende inntekt endpoint`()
//        val discardHendelseList = mutableListOf<Hendelse>()
//
//        for (i in PgiFolketrygdenErrorCodes.pgiFolketrygdenDiscardErrorCodes.indices) {
//            val hendelse = Hendelse(i + 5000L, "1111111111$i", "2019", HendelseMetadata(0))
//            pensjonsgivendeInntektMock.`stub error code from skatt`(hendelse, PgiFolketrygdenErrorCodes.pgiFolketrygdenDiscardErrorCodes[i])
//            discardHendelseList.add(hendelse)
//        }
//
//        addToHendelseTopic(TEN)
//        addToHendelseTopic(discardHendelseList)
//        addToHendelseTopic(TEN)
//
//        val output = testOutputTopic.readKeyValuesToList()
//
//        assertEquals(TEN + discardHendelseList.size + TEN, pensjonsgivendeInntektMock.callsToMock())
//        assertEquals(TEN + TEN, output.size)
//    }

    @Test
    internal fun `should fail with Exception if exception is thrown in stream`() {
        val failingHendelse = Hendelse(1L, IDENTIFIKATOR, INNTEKTSAAR, HendelseMetadata(0))

        pensjonsgivendeInntektMock.`stub pensjongivende inntekt endpoint`()
        pensjonsgivendeInntektMock.`stub 401 from skatt`(INNTEKTSAAR, IDENTIFIKATOR)

        addToHendelseTopic(TEN)

        assertThatThrownBy {
            addToTopic(failingHendelse)
        }
            .isInstanceOf(StreamsException::class.java)
            .hasRootCauseInstanceOf(FeilmedlingFraSkattException::class.java)
        assertEquals(TEN, testOutputTopic.readKeyValuesToList().size)
    }

    private fun getKafkaTestEnv() =
        mapOf(
            KafkaConfig.BOOTSTRAP_SERVERS to "test",
            KafkaConfig.SCHEMA_REGISTRY_USERNAME to "test",
            KafkaConfig.SCHEMA_REGISTRY_PASSWORD to "test",
            KafkaConfig.SCHEMA_REGISTRY to MOCK_SCHEMA_REGISTRY_URL
        )

    private fun addToHendelseTopic(amount: Int) = createHendelseList(amount).forEach { addToTopic(it) }

    private fun addToTopic(hendelse: Hendelse) {
        val key : String = PgiDomainSerializer().toJson(hendelse.key())
        val value : String = PgiDomainSerializer().toJson(hendelse)
        println("Adding to topic: $key $value")
        testInputTopic.pipeInput(key, value)
        println("Added to topic: $key $value")
    }

    private fun createHendelseList(count: Int) =
        (1..count).map { Hendelse(it.toLong(), (10000000000 + it).toString(), "2018", HendelseMetadata(0)) }
}

private fun Hendelse.key() = HendelseKey(identifikator, gjelderPeriode)





