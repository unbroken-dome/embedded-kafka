package org.unbrokendome.embedded.kafka.junit5

import org.apache.kafka.clients.admin.NewTopic


/**
 * Annotates a method that will return additional topics to be created for the current test context.
 *
 * The following return values are supported:
 * - an instance of [NewTopic]
 * - a [String] (indicating the topic name)
 * - a [Collection] of the above
 *
 * If the return value of this method is a string, or a collection of strings (containing just the topic names),
 * the `numPartitions` and `replicationFactor` parameters are taken from the properties of this annotation.
 *
 * If the annotated method returns [NewTopic], or a collection of [NewTopic]s, the properties of this
 * annotation are ignored.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class EmbeddedKafkaTopics(
        /**
         * The number of partitions for new topics returned by the annotated method. Only relevant if the
         * method returns just the topic names as [String]s.
         */
        val numPartitions: Int = DEFAULT_TOPIC_NUM_PARTITIONS,
        /**
         * The replication factor for new topics returned by the annotated method. Only relevant if the
         * method returns just the topic names as [String]s.
         */
        val replicationFactor: Short = DEFAULT_TOPIC_REPLICATION_FACTOR)
