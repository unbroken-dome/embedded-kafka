package org.unbrokendome.embedded.kafka.junit5

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.unbrokendome.embedded.support.resolveAsClass
import java.lang.reflect.ParameterizedType


abstract class AbstractKafkaClientArgumentResolver : ArgumentResolver {

    final override fun supports(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean =
            parameterContext.parameter.let { parameter ->
                val hasProducerAnnotation = parameter.isAnnotationPresent(EmbeddedKafkaProducer::class.java)
                val hasConsumerAnnotation = parameter.isAnnotationPresent(EmbeddedKafkaConsumer::class.java)

                require(!(hasProducerAnnotation && hasConsumerAnnotation)) {
                    "A parameter cannot be annotated with both @EmbeddedKafkaProducer " +
                            "and @EmbeddedKafkaConsumer"
                }

                when {
                    hasProducerAnnotation -> parameter.type in supportedAnnotatedProducerTypes
                    hasConsumerAnnotation -> parameter.type in supportedAnnotatedConsumerTypes
                    else -> parameter.type in supportedUnannotatedProducerTypes ||
                            parameter.type in supportedUnannotatedConsumerTypes
                }
            }

    protected open val supportedAnnotatedProducerTypes: Set<Class<*>>
        get() = supportedUnannotatedProducerTypes

    protected open val supportedUnannotatedProducerTypes: Set<Class<*>>
        get() = emptySet()

    protected open val supportedAnnotatedConsumerTypes: Set<Class<*>>
        get() = supportedUnannotatedConsumerTypes

    protected open val supportedUnannotatedConsumerTypes: Set<Class<*>>
        get() = emptySet()


    final override fun resolve(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any? =
            parameterContext.parameter.let { parameter ->
                if (parameter.isAnnotationPresent(EmbeddedKafkaProducer::class.java) ||
                        parameter.type in supportedUnannotatedProducerTypes) {
                    resolveProducer(parameterContext, extensionContext)

                } else if (parameter.isAnnotationPresent(EmbeddedKafkaConsumer::class.java) ||
                        parameter.type in supportedUnannotatedConsumerTypes) {
                    resolveConsumer(parameterContext, extensionContext)

                } else {
                    throw IllegalArgumentException("Cannot resolve argument for ParameterContext: $parameterContext")
                }
            }


    private fun resolveProducer(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any? {
        parameterContext.parameter.let { parameter ->
            val annotation: EmbeddedKafkaProducer? =
                    parameter.getAnnotation(EmbeddedKafkaProducer::class.java)

            val serializerProps = if (shouldGuessSerializerTypes(parameter.type)) {
                val (keyType, valueType) = getKeyAndValueType(parameterContext)
                mapOf(
                        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to
                                determineSerializerClass(annotation?.keySerializerClass?.java, keyType, true),
                        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to
                                determineSerializerClass(annotation?.valueSerializerClass?.java, valueType, false))
            } else {
                annotation.serializerProperties
            }

            return createProducer(parameterContext.parameter.type,
                    serializerProps + annotation.parseProducerProperties() + extensionContext.clientProperties)
        }
    }


    protected open fun shouldGuessSerializerTypes(parameterType: Class<*>): Boolean = false


    protected open fun shouldGuessDeserializerTypes(parameterType: Class<*>): Boolean = false


    protected open fun createProducer(expectedType: Class<*>, producerProps: Map<String, Any>): Any? = null


    private fun resolveConsumer(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any? {
        parameterContext.parameter.let { parameter ->
            val annotation: EmbeddedKafkaConsumer? =
                    parameter.getAnnotation(EmbeddedKafkaConsumer::class.java)

            val deserializerProps = if (shouldGuessDeserializerTypes(parameter.type)) {
                val (keyType, valueType) = getKeyAndValueType(parameterContext)
                mapOf(
                        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to
                                determineDeserializerClass(annotation?.keyDeserializerClass?.java, keyType, true),
                        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to
                                determineDeserializerClass(annotation?.valueDeserializerClass?.java, valueType, false))
            } else {
                annotation.deserializerProperties
            }

            return createConsumer(parameter.type,
                    deserializerProps + annotation.parseConsumerProperties() + extensionContext.clientProperties,
                    annotation?.topics?.toList() ?: emptyList())
        }
    }


    protected open fun createConsumer(expectedType: Class<*>,
                                      consumerProps: Map<String, Any>,
                                      topics: List<String>): Any? = null


    private fun getKeyAndValueType(parameterContext: ParameterContext): Pair<Class<*>?, Class<*>?> =
            (parameterContext.parameter.parameterizedType as? ParameterizedType)
                    ?.actualTypeArguments
                    ?.takeIf { it.size == 2 }
                    ?.map { it.resolveAsClass() }
                    ?.let {
                        it[0] to it[1]
                    } ?: (null to null)


    private fun determineSerializerClass(serializerClassFromAnnotation: Class<out Serializer<*>>?,
                                         itemClass: Class<*>?, isKey: Boolean): Class<out Serializer<*>> =
    // If the serializer class is specified explicitly on the annotation, use it
            serializerClassFromAnnotation?.takeIf { it != Serializer::class.java }
            // Otherwise, if we have a type parameter for key/value, try to guess the correct serializer
                    ?: itemClass?.let {
                        DefaultSerializerTypeGuesser.guessSerializerType(it, isKey)
                    }
                            // If we didn't find a serializer, complain with an exception
                            .let {
                                requireNotNull(it) {
                                    val typeDescription = itemClass?.let { "type ${it.name}" } ?: "unknown type"
                                    "Could not determine the serializer class for $typeDescription, " +
                                            "please specify it explicitly in the @EmbeddedKafkaProducer annotation."
                                }
                            }


    private fun determineDeserializerClass(deserializerClassFromAnnotation: Class<out Deserializer<*>>?,
                                           itemClass: Class<*>?, isKey: Boolean): Class<out Deserializer<*>> =
    // If the deserializer class is specified explicitly on the annotation, use it
            deserializerClassFromAnnotation?.takeIf { it != Deserializer::class.java }
            // Otherwise, if we have a type parameter for key/value, try to guess the correct deserializer
                    ?: itemClass?.let {
                        DefaultSerializerTypeGuesser.guessDeserializerType(it, isKey)
                    }
                            // If we didn't find a deserializer, complain with an exception
                            .let {
                                requireNotNull(it) {
                                    val typeDescription = itemClass?.let { "type ${it.name}" } ?: "unknown type"
                                    "Could not determine the deserializer class for $typeDescription, " +
                                            "please specify it explicitly in the @EmbeddedKafkaConsumer annotation."
                                }
                            }
}
