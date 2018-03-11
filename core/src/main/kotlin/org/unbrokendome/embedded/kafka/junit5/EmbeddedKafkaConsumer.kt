package org.unbrokendome.embedded.kafka.junit5

import org.apache.kafka.common.serialization.Deserializer
import kotlin.reflect.KClass

import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.Serializer
import org.unbrokendome.embedded.support.parsePropertiesFromList

/**
 * Indicates that the annotated parameter should be injected with a Kafka consumer.
 *
 * The consumer will be configured to connect to the embedded Kafka broker, and will be automatically closed
 * after the test.
 *
 * The following parameter types are supported by default:
 * - [Consumer]
 * - [KafkaConsumer]
 * - `Map<String, ?>` (containing the configuration options for a consumer)
 *
 * Support for additional parameter types may be added by creating an implementation of [KafkaClientParameterResolver]
 * and registering it via the [java.util.ServiceLoader] mechanism.
 */
annotation class EmbeddedKafkaConsumer(
        /**
         * Additional configuration properties to pass to the consumer.
         *
         * Each item should be a property in `key=value` syntax (similar to the Java properties file format).
         */
        val properties: Array<String> = [],
        /**
         * The class of the key deserializer.
         *
         * In many cases this can automatically be guessed from the
         * type parameters of the consumer parameter type.
         */
        val keyDeserializerClass: KClass<out Deserializer<*>> = Deserializer::class,
        /**
         * The class of the value deserializer.
         *
         * In many cases this can automatically be guessed from the
         * type parameters of the consumer parameter type.
         */
        val valueDeserializerClass: KClass<out Deserializer<*>> = Deserializer::class,
        /**
         * A list of topics that the consumer should be subscribed to. Only applies if the parameter type actually
         * represents a fully configured consumer.
         */
        val topics: Array<String> = [])


internal fun EmbeddedKafkaConsumer?.parseConsumerProperties(): Map<String, Any> =
        this?.properties?.asList()?.let {
                parsePropertiesFromList(it)
        } ?: emptyMap()


internal val EmbeddedKafkaConsumer?.deserializerProperties: Map<String, Any>
        get() = this?.run {
                mapOf(
                        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to keyDeserializerClass.java,
                        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to valueDeserializerClass.java)
                        .filterValues { it != Deserializer::class.java }
        } ?: emptyMap()
