package org.unbrokendome.embedded.support

import java.net.ServerSocket
import java.util.function.Supplier


/**
 * Provides a random free TCP port on the local host.
 */
object RandomPortSupplier : Supplier<Int> {

    override fun get(): Int =
            ServerSocket(0).use {
                it.localPort
            }
}
