package no.nav.pgi.skatt.inntekt.mock

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.TestInputTopic
import org.apache.kafka.streams.TestOutputTopic
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.TopologyTestDriver
import java.util.*

internal class PgiTopologyTestDriver(topology: Topology, properties: Properties) {
    internal val testDriver: TopologyTestDriver = TopologyTestDriver(topology, properties)

    internal fun <Key : SpecificRecord, Value : SpecificRecord> createInputTopic(topic: String, schemaUrl: String): TestInputTopic<Key, Value> {
        return testDriver.createInputTopic(topic, createSerde<Key>(schemaUrl).serializer(), createSerde<Value>(schemaUrl).serializer())
    }

    internal fun <Key : SpecificRecord, Value : SpecificRecord> createOutputTopic(topic: String, schemaUrl: String): TestOutputTopic<Key, Value> {
        return testDriver.createOutputTopic(topic, createSerde<Key>(schemaUrl).deserializer(), createSerde<Value>(schemaUrl).deserializer())
    }

    internal fun close() = testDriver.close()

    private fun <AvroClass : SpecificRecord> createSerde(schemaUrl: String): Serde<AvroClass> {
        val serde: Serde<AvroClass> = SpecificAvroSerde()
        serde.configure(mapOf(SCHEMA_REGISTRY_URL_CONFIG to schemaUrl), false)
        return serde
    }

    companion object {
        private val SCHEMA_REGISTRY_SCOPE: String = PgiTopologyTestDriver::class.java.name
        internal val MOCK_SCHEMA_REGISTRY_URL = "mock://$SCHEMA_REGISTRY_SCOPE"
    }
}