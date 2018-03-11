package org.unbrokendome.embedded.kafka.junit5


/**
 * Helper class that contains a "disposable" value for a parameter (i.e. a value that requires to be
 * cleaned up after a test), as well as a strategy for disposal.
 *
 * This is intended for cases where the injected parameter value needs some cleanup, but does not implement the
 * [AutoCloseable] interface.
 */
class DisposableParameter<out T : Any>(
        val value: T,
        private val disposer: T.() -> Unit) : AutoCloseable {

    override fun close() {
        disposer(value)
    }
}
