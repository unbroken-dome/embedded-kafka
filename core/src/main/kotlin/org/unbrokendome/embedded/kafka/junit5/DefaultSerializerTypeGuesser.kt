package org.unbrokendome.embedded.kafka.junit5

import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer
import java.util.ServiceLoader


/**
 * Default implementation of [SerializerTypeGuesser].
 *
 * This delegates to all [SerializerTypeGuesser] implementations that are available via the [ServiceLoader]
 * mechanism.
 */
object DefaultSerializerTypeGuesser : SerializerTypeGuesser {

    private val guessers = ServiceLoader.load(SerializerTypeGuesser::class.java)


    override fun guessSerializerType(type: Class<*>, isKey: Boolean): Class<out Serializer<*>>? =
            guessers.map { it.guessSerializerType(type, isKey) }
                    .firstOrNull { it != null }


    override fun guessDeserializerType(type: Class<*>, isKey: Boolean): Class<out Deserializer<*>>? =
            guessers.map { it.guessDeserializerType(type, isKey) }
                    .firstOrNull { it != null }
}
