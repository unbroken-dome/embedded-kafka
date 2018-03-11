package org.unbrokendome.embedded.kafka.junit5

import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer


/**
 * Strategy interface for "guessing" the type of a [Serializer] or [Deserializer] based on the key or value type
 * of an injected producer or consumer.
 *
 * This is used if a producer or consumer is injected and serializer/deserializer types are not explicitly
 * provided in the [EmbeddedKafkaProducer] or [EmbeddedKafkaConsumer] annotation.
 */
interface SerializerTypeGuesser {

    /**
     * Determines a serializer type for the given type.
     *
     * @param type the type to be serialized
     * @param isKey `true` if the type is used as a key
     *
     * @return the [Serializer] implementation type, or `null` if none can be determined
     */
    fun guessSerializerType(type: Class<*>, isKey: Boolean): Class<out Serializer<*>>?

    /**
     * Determines a deserializer type for the given type.
     *
     * @param type the type to be deserialized
     * @param isKey `true` if the type is used as a key
     *
     * @return the [Deserializer] implementation type, or `null` if none can be determined
     */
    fun guessDeserializerType(type: Class<*>, isKey: Boolean): Class<out Deserializer<*>>?
}
