package org.unbrokendome.embedded.kafka.junit5

import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.Serializer
import org.unbrokendome.embedded.support.parsePropertiesFromList
import kotlin.reflect.KClass


/**
 * Indicates that the annotated parameter should be injected with a Kafka producer.
 *
 * The producer will be configured to connect to the embedded Kafka broker, and will be automatically closed
 * after the test.
 *
 * The following parameter types are supported by default:
 * - [Producer]
 * - [KafkaProducer]
 * - `Map<String, ?>` (containing the configuration options for a producer)
 *
 * Support for additional parameter types may be added by creating an implementation of [KafkaClientParameterResolver]
 * and registering it via the [java.util.ServiceLoader] mechanism.
 */
annotation class EmbeddedKafkaProducer(
        /**
         * Additional configuration properties to pass to the producer.
         *
         * Each item should be a property in `key=value` syntax (similar to the Java properties file format).
         */
        val properties: Array<String> = [],
        /**
         * The class of the key serializer.
         *
         * In many cases this can automatically be guessed from the
         * type parameters of the producer parameter type.
         */
        val keySerializerClass: KClass<out Serializer<*>> = Serializer::class,
        /**
         * The class of the value serializer.
         *
         * In many cases this can automatically be guessed from the
         * type parameters of the producer parameter type.
         */
        val valueSerializerClass: KClass<out Serializer<*>> = Serializer::class)


internal fun EmbeddedKafkaProducer?.parseProducerProperties(): Map<String, Any> =
        this?.properties?.asList()?.let {
                parsePropertiesFromList(it)
        } ?: emptyMap()


internal val EmbeddedKafkaProducer?.serializerProperties: Map<String, Any>
    get() = this?.run {
        mapOf(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to keySerializerClass.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to valueSerializerClass.java)
                .filterValues { it != Serializer::class.java }
    } ?: emptyMap()
