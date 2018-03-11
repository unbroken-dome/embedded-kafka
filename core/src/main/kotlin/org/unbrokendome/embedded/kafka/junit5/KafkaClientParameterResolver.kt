package org.unbrokendome.embedded.kafka.junit5

import org.junit.jupiter.api.extension.ParameterResolver


/**
 * Strategy interface for resolving injectable parameters for Kafka clients.
 *
 * This interface extends JUnit's [ParameterResolver] but does not add any methods; it is intended mostly as
 * a marker interface to be used with [java.util.ServiceLoader].
 */
interface KafkaClientParameterResolver : ParameterResolver
