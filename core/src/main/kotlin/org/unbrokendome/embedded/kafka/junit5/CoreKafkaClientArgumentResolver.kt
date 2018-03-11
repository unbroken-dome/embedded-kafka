package org.unbrokendome.embedded.kafka.junit5

import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer


class CoreKafkaClientArgumentResolver : AbstractKafkaClientArgumentResolver() {

    override val supportedAnnotatedProducerTypes: Set<Class<*>> =
            setOf(Producer::class.java, KafkaProducer::class.java, Map::class.java)

    override val supportedUnannotatedProducerTypes: Set<Class<*>> =
            setOf(Producer::class.java, KafkaProducer::class.java)

    override val supportedAnnotatedConsumerTypes: Set<Class<*>> =
            setOf(Consumer::class.java, KafkaConsumer::class.java, Map::class.java)

    override val supportedUnannotatedConsumerTypes: Set<Class<*>> =
            setOf(Consumer::class.java, KafkaConsumer::class.java)


    override fun shouldGuessSerializerTypes(parameterType: Class<*>): Boolean =
            parameterType.isAssignableFrom(KafkaProducer::class.java)


    override fun shouldGuessDeserializerTypes(parameterType: Class<*>): Boolean =
            parameterType.isAssignableFrom(KafkaConsumer::class.java)


    override fun createProducer(expectedType: Class<*>, producerProps: Map<String, Any>): Any? =
            when {
                expectedType.isAssignableFrom(KafkaProducer::class.java) ->
                    KafkaProducer<Any, Any>(producerProps)

                expectedType == Map::class.java ->
                    producerProps

                else -> null
            }


    override fun createConsumer(expectedType: Class<*>, consumerProps: Map<String, Any>,
                                topics: List<String>): Any? =
            when {
                expectedType.isAssignableFrom(KafkaConsumer::class.java) ->
                    KafkaConsumer<Any, Any>(consumerProps)
                            .let { consumer ->
                                if (topics.isNotEmpty()) {
                                    consumer.subscribe(topics)
                                    DisposableParameter(consumer) {
                                        unsubscribe()
                                        close()
                                    }
                                } else {
                                    consumer
                                }
                            }

                expectedType == Map::class.java ->
                    consumerProps

                else -> null
            }
}
