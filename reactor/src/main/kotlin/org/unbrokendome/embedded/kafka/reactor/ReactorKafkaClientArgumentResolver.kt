package org.unbrokendome.embedded.kafka.reactor

import org.unbrokendome.embedded.kafka.junit5.AbstractKafkaClientArgumentResolver
import org.unbrokendome.embedded.kafka.junit5.DisposableParameter
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions


class ReactorKafkaClientArgumentResolver : AbstractKafkaClientArgumentResolver() {

    override val supportedUnannotatedProducerTypes: Set<Class<*>> =
            setOf(KafkaSender::class.java, SenderOptions::class.java)

    override val supportedUnannotatedConsumerTypes: Set<Class<*>> =
            setOf(KafkaReceiver::class.java, ReceiverOptions::class.java)


    override fun shouldGuessSerializerTypes(parameterType: Class<*>): Boolean = true


    override fun shouldGuessDeserializerTypes(parameterType: Class<*>): Boolean = true


    override fun createProducer(expectedType: Class<*>, producerProps: Map<String, Any>): Any? =
            SenderOptions.create<Any, Any>(producerProps)
                    .let { senderOptions ->
                        if (expectedType == SenderOptions::class.java) {
                            senderOptions
                        } else {
                            KafkaSender.create(senderOptions)
                                    .let {
                                        DisposableParameter(it) { it.close() }
                                    }
                        }
                    }


    override fun createConsumer(expectedType: Class<*>, consumerProps: Map<String, Any>, topics: List<String>): Any? =
            ReceiverOptions.create<Any, Any>(consumerProps)
                    .subscription(topics)
                    .let { receiverOptions ->
                        if (expectedType == ReceiverOptions::class.java) {
                            receiverOptions

                        } else {
                            KafkaReceiver.create(receiverOptions)
                        }
                    }
}
