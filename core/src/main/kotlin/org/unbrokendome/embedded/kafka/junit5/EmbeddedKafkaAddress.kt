package org.unbrokendome.embedded.kafka.junit5

/**
 * Indicates that the annotated parameter should be injected with the address of the embedded Kafka broker.
 *
 * The type of the parameter can be either [String] or [Int].
 *
 * If the annotated parameter is a [String], it will receive the full address
 * of the broker, which can be used for example in the `bootstrap.servers` property for various Kafka client
 * configurations.
 *
 * If the annotated parameter is an [Int], it will receive only the port number on which the Kafka broker
 * is listening.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class EmbeddedKafkaAddress
