package org.unbrokendome.embedded.kafka.junit5

import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.TimeUnit


@ExtendWith(EmbeddedKafkaExtension::class)
class EmbeddedKafkaExtensionNestedTest {

    @Nested
    @DisplayName("with automatically created topic")
    inner class WithAutoTopic {

        @Test
        @DisplayName("send a record")
        fun testSend(@EmbeddedKafkaAddress kafkaAddress: String) {
            createProducer(kafkaAddress)
                    .use { kafkaProducer ->
                        val record = ProducerRecord<String, String>("test-topic", "key", "value")
                        kafkaProducer.send(record)
                                .get(5L, TimeUnit.SECONDS)
                    }
        }
    }


    @Nested
    @DisplayName("with explicitly created topic")
    inner class WithExplicitTopic {

        @EmbeddedKafkaProperties
        fun brokerProperties() = mapOf(
                "auto.create.topics.enable" to false)

        @EmbeddedKafkaTopics
        fun topics() = listOf(
                NewTopic("test-topic", 3, 1))


        @Test
        @DisplayName("send a record")
        fun testSend(@EmbeddedKafkaAddress kafkaAddress: String) {
            createProducer(kafkaAddress)
                    .use { kafkaProducer ->

                        val record = ProducerRecord<String, String>("test-topic", "key", "value")
                        kafkaProducer.send(record)
                                .get(5L, TimeUnit.SECONDS)
                    }
        }
    }


    private fun createProducer(kafkaAddress: String): Producer<String, String> =
            KafkaProducer<String, String>(mapOf(
                    ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaAddress,
                    ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                    ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java))
}