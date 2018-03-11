package org.unbrokendome.embedded.kafka.junit5

import org.junit.jupiter.api.Test

@EmbeddedKafka
class EmbeddedKafkaAnnotationTest {

    @Test
    @EmbeddedKafka(
            brokerProperties = ["auto.create.topics.enable=false"])
    fun test() {


    }
}