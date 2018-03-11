package org.unbrokendome.embedded.kafka.spring

import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaOperations
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.unbrokendome.embedded.kafka.junit5.AbstractKafkaClientArgumentResolver


class SpringKafkaClientArgumentResolver : AbstractKafkaClientArgumentResolver() {

    override val supportedUnannotatedProducerTypes: Set<Class<*>> =
            setOf(KafkaTemplate::class.java, KafkaOperations::class.java,
                    ProducerFactory::class.java)

    override val supportedUnannotatedConsumerTypes: Set<Class<*>> =
            setOf(ConsumerFactory::class.java)


    override fun shouldGuessSerializerTypes(parameterType: Class<*>): Boolean = true

    override fun shouldGuessDeserializerTypes(parameterType: Class<*>): Boolean = true


    override fun createProducer(expectedType: Class<*>, producerProps: Map<String, Any>): Any? =
            DefaultKafkaProducerFactory<Any, Any>(producerProps).let { producerFactory ->
                if (expectedType.isAssignableFrom(KafkaTemplate::class.java)) {
                    KafkaTemplate(producerFactory)
                } else {
                    producerFactory
                }
            }


    override fun createConsumer(expectedType: Class<*>, consumerProps: Map<String, Any>,
                                topics: List<String>): Any? =
            DefaultKafkaConsumerFactory<Any, Any>(consumerProps)
}