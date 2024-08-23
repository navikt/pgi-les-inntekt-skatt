package no.nav.pgi.skatt.inntekt.mock

import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.streams.TestInputTopic
import org.apache.kafka.streams.TestOutputTopic
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.TopologyTestDriver
import java.util.*

internal class PgiTopologyTestDriver(topology: Topology, properties: Properties) {
    internal val testDriver: TopologyTestDriver = TopologyTestDriver(topology, properties)

    internal fun createInputTopic(topic: String, schemaUrl: String): TestInputTopic<String, String> {
        return testDriver.createInputTopic(topic, StringSerializer(), StringSerializer())
    }

    internal fun createOutputTopic(topic: String, schemaUrl: String): TestOutputTopic<String, String> {
        return testDriver.createOutputTopic(topic, StringDeserializer(), StringDeserializer())
    }

    internal fun close() = testDriver.close()

    companion object {
        private val SCHEMA_REGISTRY_SCOPE: String = PgiTopologyTestDriver::class.java.name
        internal val MOCK_SCHEMA_REGISTRY_URL = "mock://$SCHEMA_REGISTRY_SCOPE"
    }
}