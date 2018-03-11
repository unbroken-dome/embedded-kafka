package org.unbrokendome.embedded.kafka.junit5

import org.apache.kafka.clients.admin.NewTopic
import org.junit.jupiter.api.extension.ExtendWith
import org.unbrokendome.embedded.support.parsePropertiesFromList
import java.util.Optional


/**
 * Activates the embedded Kafka broker for this test.
 *
 * Can be placed on a test method or a type, and supports `@Nested` test classes as well.
 * If the annotation is placed on multiple levels, the properties on the lowest level override the one
 * from higher levels (e.g. a method annotation overrides a class annotation, and a @Nested class annotation
 * overrides an annotation on the containing class).
 *
 * This annotation is meta-annotated with [ExtendWith], so you do not have to specify
 * `@ExtendWith(EmbeddedKafkaExtension.class)` again.
 *
 * It is recommended to use this annotation instead of `@ExtendWith(EmbeddedKafkaExtension.class)` because it
 * allows additional configuration options.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@ExtendWith(EmbeddedKafkaExtension::class)
annotation class EmbeddedKafka(
        /**
         * Additional configuration properties for the embedded broker.
         *
         * See [Broker Configs](https://kafka.apache.org/documentation/#brokerconfigs) in the Kafka documentation
         * for possible values.
         *
         * Note that some properties (like `listeners` or `log.dir`) cannot be overridden, as they are managed by
         * the embedded Kafka engine.
         */
        val brokerProperties: Array<String> = [],
        /**
         * Whether to expose Java system properties containing the addresses of the embedded Kafka broker and
         * Zookeeper server.
         *
         * If `true`, the following system properties will be exposed:
         * - `embedded.kafka.bootstrap.servers` - Address of the embedded Kafka broker; clients can use this
         *   value for the `bootstrap.servers` configuration property
         * - `embedded.zookeeper.connect` - Address of the embedded Zookeeper server.
         */
        val exposeSystemProperties: Boolean = false,
        /**
         * Contains a list of topics to be created.
         */
        val createTopics: Array<String> = [],
        /**
         * The number of partitions for new topics given in the [createTopics] property.
         */
        val topicPartitions: Int = DEFAULT_TOPIC_NUM_PARTITIONS,
        /**
         * The replication factor for new topics given in the [createTopics] property.
         */
        val topicReplicationFactor: Short = DEFAULT_TOPIC_REPLICATION_FACTOR)


/**
 * Parses the broker properties from an [EmbeddedKafka] annotation as a map.
 *
 * @return a [Map] containing the parsed broker properties
 */
internal fun EmbeddedKafka.parseBrokerProperties() =
        parsePropertiesFromList(brokerProperties.asIterable())


/**
 * Parses the broker properties from an optional [EmbeddedKafka] annotation as a map.
 *
 * Returns an empty map if the annotation is absent.
 *
 * @return a [Map] containing the parsed broker properties
 */
internal fun Optional<EmbeddedKafka>.parseBrokerProperties() =
        map { it.parseBrokerProperties() }
                .orElse(emptyMap())


/**
 * Gets a list of [NewTopic] instances from an [EmbeddedKafka] annotation.
 *
 * @return a [List] of [NewTopic] instances
 */
internal fun EmbeddedKafka.getNewTopics() =
        createTopics.map { topicName ->
            NewTopic(topicName, topicPartitions, topicReplicationFactor)
        }


/**
 * Gets a list of [NewTopic] instances for an optional [EmbeddedKafka] annotation.
 *
 * Returns an empty list if the annotation is absent.
 *
 * @return a [List] of [NewTopic] instances
 */
internal fun Optional<EmbeddedKafka>.getNewTopics() =
        map { it.getNewTopics() }
                .orElse(emptyList())

/**
 * The default number of partitions for new topics.
 */
internal const val DEFAULT_TOPIC_NUM_PARTITIONS = 1

/**
 * The default replication factor for new topics.
 */
internal const val DEFAULT_TOPIC_REPLICATION_FACTOR = 1.toShort()
