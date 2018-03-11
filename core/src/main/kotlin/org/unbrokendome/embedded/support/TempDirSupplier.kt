package org.unbrokendome.embedded.support

import com.google.common.io.MoreFiles
import com.google.common.io.RecursiveDeleteOption
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.function.Supplier


/**
 * Supplier for temporary directories.
 *
 * Keeps track of all the temporary directories that were created from this instance, and deletes them
 * when it is closed.
 */
class TempDirSupplier(
        private val prefix: String)
    : Supplier<File>, AutoCloseable {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val directories = mutableListOf<Path>()


    override fun get(): File =
        Files.createTempDirectory(prefix)
                .also { directories.add(it) }
                .toFile()


    override fun close() {
        directories.forEach {
            logger.debug("Deleting temporary directory: {}", it)
            MoreFiles.deleteRecursively(it, RecursiveDeleteOption.ALLOW_INSECURE)
        }
    }
}
