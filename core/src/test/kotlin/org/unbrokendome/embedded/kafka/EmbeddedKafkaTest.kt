package org.unbrokendome.embedded.kafka

import org.junit.jupiter.api.Test
import org.unbrokendome.embedded.zookeeper.EmbeddedZookeeper
import java.util.function.Supplier


class EmbeddedKafkaTest {

    @Test
    fun testStartStop() {
        EmbeddedZookeeper().use { zookeeper ->

            zookeeper.startAsync()
                    .awaitRunning()

            try {

                EmbeddedKafka(Supplier { zookeeper.zkConnect }).use { kafka ->
                    kafka.startAsync()
                            .awaitRunning()
                    try {

                    } finally {
                        kafka.stopAsync()
                                .awaitTerminated()
                    }
                }

            } finally {
                zookeeper.stopAsync()
                        .awaitTerminated()
            }
        }
    }
}