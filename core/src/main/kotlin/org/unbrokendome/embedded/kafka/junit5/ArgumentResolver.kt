package org.unbrokendome.embedded.kafka.junit5

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext


interface ArgumentResolver {

    fun supports(parameterContext: ParameterContext,
                 extensionContext: ExtensionContext): Boolean

    fun resolve(parameterContext: ParameterContext,
                extensionContext: ExtensionContext): Any?
}
