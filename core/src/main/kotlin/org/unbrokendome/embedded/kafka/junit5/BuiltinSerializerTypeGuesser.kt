package org.unbrokendome.embedded.kafka.junit5

import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.ByteBufferDeserializer
import org.apache.kafka.common.serialization.ByteBufferSerializer
import org.apache.kafka.common.serialization.BytesDeserializer
import org.apache.kafka.common.serialization.BytesSerializer
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.DoubleDeserializer
import org.apache.kafka.common.serialization.DoubleSerializer
import org.apache.kafka.common.serialization.FloatDeserializer
import org.apache.kafka.common.serialization.FloatSerializer
import org.apache.kafka.common.serialization.IntegerDeserializer
import org.apache.kafka.common.serialization.IntegerSerializer
import org.apache.kafka.common.serialization.LongDeserializer
import org.apache.kafka.common.serialization.LongSerializer
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.common.serialization.ShortDeserializer
import org.apache.kafka.common.serialization.ShortSerializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.common.utils.Bytes
import java.nio.ByteBuffer


/**
 * Implementation of [SerializerTypeGuesser] that provides serializers and deserializers the built-in "simple"
 * types.
 */
class BuiltinSerializerTypeGuesser : SerializerTypeGuesser {

    override fun guessSerializerType(type: Class<*>, isKey: Boolean): Class<out Serializer<*>>? =
        when (type) {
            String::class.java -> StringSerializer::class.java
            ByteArray::class.java -> ByteArraySerializer::class.java
            ByteBuffer::class.java -> ByteBufferSerializer::class.java
            Short::class.java -> ShortSerializer::class.java
            Int::class.java -> IntegerSerializer::class.java
            Long::class.java -> LongSerializer::class.java
            Float::class.java -> FloatSerializer::class.java
            Double::class.java -> DoubleSerializer::class.java
            Bytes::class.java -> BytesSerializer::class.java
            else -> null
        }

    override fun guessDeserializerType(type: Class<*>, isKey: Boolean): Class<out Deserializer<*>>? =
            when (type) {
                String::class.java -> StringDeserializer::class.java
                ByteArray::class.java -> ByteArrayDeserializer::class.java
                ByteBuffer::class.java -> ByteBufferDeserializer::class.java
                Short::class.java -> ShortDeserializer::class.java
                Int::class.java -> IntegerDeserializer::class.java
                Long::class.java -> LongDeserializer::class.java
                Float::class.java -> FloatDeserializer::class.java
                Double::class.java -> DoubleDeserializer::class.java
                Bytes::class.java -> BytesDeserializer::class.java
                else -> null
            }
}
