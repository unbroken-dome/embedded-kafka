package org.unbrokendome.embedded.kafka.junit5

/**
 * Annotates a method that will provide additional broker properties for the current test context.
 *
 * The return type of the method should be a [Map] with string keys.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class EmbeddedKafkaProperties(
        /**
         * Indicates whether the properties returned by the annotated method should be merged with (`true`)
         * or replace (`false`) any properties from the parent context.
         */
        val merge: Boolean = true)
