package org.unbrokendome.embedded.kafka.reactor

import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.unbrokendome.embedded.kafka.junit5.EmbeddedKafka
import org.unbrokendome.embedded.kafka.junit5.EmbeddedKafkaConsumer
import reactor.kafka.receiver.KafkaReceiver
import reactor.test.StepVerifier
import java.time.Duration


@EmbeddedKafka(createTopics = ["test-topic"])
class KafkaReceiverTest {

    @Test
    fun testReceiver(
            producer: Producer<String, String>,
            @EmbeddedKafkaConsumer(topics = ["test-topic"],
                    properties = ["group.id=test", "auto.offset.reset=earliest"])
            receiver: KafkaReceiver<String, String>) {

        StepVerifier.create(receiver.receive())
                .then {
                    val record = ProducerRecord<String, String>("test-topic", "KEY", "VALUE")
                    producer.send(record)
                            .get()
                }
                .assertNext {
                    assertEquals("KEY", it.key())
                    assertEquals("VALUE", it.value())
                }
                // the KafkaReceiver will never complete, we need to cancel explicitly
                .thenCancel()
                // always use a timeout, in case we don't receive anything
                .verify(Duration.ofSeconds(5))
    }
}
