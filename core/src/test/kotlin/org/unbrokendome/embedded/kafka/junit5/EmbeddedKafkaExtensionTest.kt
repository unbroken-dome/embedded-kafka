package org.unbrokendome.embedded.kafka.junit5

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.TimeUnit


@ExtendWith(EmbeddedKafkaExtension::class)
class EmbeddedKafkaExtensionTest {

    @get:EmbeddedKafkaProperties
    val brokerProperties
        get() = mapOf(
                "auto.create.topics.enable" to false)

    @get:EmbeddedKafkaTopics
    val topics
        get() = listOf("test-topic")


    @Test
    fun simpleKafkaTest(@EmbeddedKafkaAddress kafkaAddress: String) {
        KafkaProducer<String, String>(mapOf(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaAddress,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java))
                .use { kafkaProducer ->

                    val record = ProducerRecord<String, String>("test-topic", "key", "value")
                    kafkaProducer.send(record)
                            .get(5L, TimeUnit.SECONDS)
                }
    }


    @Test
    @DisplayName("inject KafkaProducer as parameter")
    fun testInjectedProducer(producer: Producer<String, String>) {
        val record = ProducerRecord<String, String>("test-topic", "key", "value")
        producer.send(record)
                .get(5L, TimeUnit.SECONDS)
    }


    @Test
    @DisplayName("inject KafkaProducer as annotated parameter")
    fun testInjectedProducerWithAnnotation(@EmbeddedKafkaProducer(valueSerializerClass = StringSerializer::class)
                                           producer: Producer<String, String>) {
        val record = ProducerRecord<String, String>("test-topic", "key", "value")
        producer.send(record)
                .get(5L, TimeUnit.SECONDS)
    }
}
