package no.nav.pgi.skatt.inntekt

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import no.nav.pensjon.samhandling.liveness.IS_READY_PATH
import no.nav.pgi.skatt.inntekt.common.KafkaTestEnvironment
import no.nav.pgi.skatt.inntekt.common.PlaintextStrategy
import no.nav.pgi.skatt.inntekt.mock.MaskinportenMock
import no.nav.pgi.skatt.inntekt.mock.PensjonsgivendeInntektMock
import no.nav.pgi.skatt.inntekt.skatt.PgiClient
import no.nav.pgi.skatt.inntekt.stream.KafkaConfig
import no.nav.samordning.pgi.schema.Hendelse
import no.nav.samordning.pgi.schema.HendelseKey
import no.nav.samordning.pgi.schema.HendelseMetadata
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.net.URI
import java.net.http.HttpClient.newHttpClient
import java.net.http.HttpRequest.newBuilder
import java.net.http.HttpResponse.BodyHandlers.ofString

private const val INNTEKTSAAR = "2020"
private const val NORSK_PERSONIDENTIFIKATOR = "12345678901"

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ShutDownTest {
    /*
    private val kafkaTestEnvironment = KafkaTestEnvironment()
    private val kafkaConfig = KafkaConfig(kafkaTestEnvironment.testConfiguration(), PlaintextStrategy())
    private val pensjonsgivendeInntektMock = PensjonsgivendeInntektMock()
    private val maskinportenMock = MaskinportenMock()
    private val pgiClient = PgiClient(PensjonsgivendeInntektMock.PGI_CLIENT_ENV_VARIABLES + MaskinportenMock.MASKINPORTEN_CLIENT_ENV_VARIABLES)
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
    fun `Stream should shut down when 401 is returned from skatt`() {
        val failingHendelse = Hendelse(12345L, NORSK_PERSONIDENTIFIKATOR, INNTEKTSAAR, HendelseMetadata(0))

        pensjonsgivendeInntektMock.`stub pensjongivende inntekt endpoint`()
        pensjonsgivendeInntektMock.`stub 401 from skatt`(INNTEKTSAAR, NORSK_PERSONIDENTIFIKATOR)

        createHendelseList(10).forEach { writeToTopic(it) }
        assertEquals(200, callIsReady().statusCode())

        writeToTopic(failingHendelse)
        createHendelseList(10).forEach { writeToTopic(it) }

        GlobalScope.launch {
            assertEquals(500, callIsReady().statusCode())
            delay(3000)
            assertEquals(10, kafkaTestEnvironment.consumeInntektTopic().size)
        }

        kafkaTestEnvironment.pgiHendelseTopicOffsett()
    }



    private fun writeToTopic(hendelse: Hendelse) = kafkaTestEnvironment.writeHendelse(hendelse.key(), hendelse)
    private fun createHendelseList(count: Int) = (1..count).map { Hendelse(it.toLong(), (10000000000 + it).toString(), "2018", HendelseMetadata(0)) }
    private fun callIsReady() = newHttpClient().send(newBuilder(URI("http://localhost:8080$IS_READY_PATH")).build(), ofString())

     */
}

private fun Hendelse.key() = HendelseKey(getIdentifikator(), getGjelderPeriode())

