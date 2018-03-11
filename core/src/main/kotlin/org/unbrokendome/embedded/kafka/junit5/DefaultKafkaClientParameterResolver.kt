package org.unbrokendome.embedded.kafka.junit5

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import java.util.ServiceLoader


/**
 * Default implementation of [KafkaClientParameterResolver].
 *
 * This delegates to all [KafkaClientParameterResolver] implementations that are available via the
 * [ServiceLoader] mechanism.
 */
object DefaultKafkaClientParameterResolver : KafkaClientParameterResolver {

    private val resolvers = ServiceLoader.load(KafkaClientParameterResolver::class.java)


    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean =
            resolvers.any { it.supportsParameter(parameterContext, extensionContext) }


    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any? =
            resolvers
                    .filter { it.supportsParameter(parameterContext, extensionContext) }
                    .map { it.resolveParameter(parameterContext, extensionContext) }
                    .firstOrNull()
}
