package org.unbrokendome.embedded.kafka.junit5

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext


/**
 * Resolves method arguments for parameters annotated with [EmbeddedKafkaAddress].
 */
class KafkaAddressArgumentResolver : ArgumentResolver {

    override fun supports(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean =
            parameterContext.parameter
                    .isAnnotationPresent(EmbeddedKafkaAddress::class.java)


    override fun resolve(parameterContext: ParameterContext,
                         extensionContext: ExtensionContext): Any? =
            extensionContext.embeddedKafka.let { embeddedKafka ->
                when (parameterContext.parameter.type) {
                    String::class.java ->
                        embeddedKafka.bootstrapServers
                    Int::class.java ->
                        embeddedKafka.port
                    else ->
                        throw IllegalArgumentException("Parameter annotated with @EmbeddedKafkaAddress must " +
                                "be of type String or Int")
                }
            }
}
