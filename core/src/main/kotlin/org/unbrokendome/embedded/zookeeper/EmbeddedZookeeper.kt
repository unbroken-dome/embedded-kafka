package org.unbrokendome.embedded.zookeeper

import com.google.common.util.concurrent.AbstractService
import org.apache.zookeeper.server.ContainerManager
import org.apache.zookeeper.server.RequestProcessor
import org.apache.zookeeper.server.ServerCnxnFactory
import org.apache.zookeeper.server.ServerConfig
import org.apache.zookeeper.server.ZooKeeperServer
import org.apache.zookeeper.server.quorum.QuorumPeerConfig
import org.slf4j.LoggerFactory
import org.unbrokendome.embedded.support.RandomPortSupplier
import org.unbrokendome.embedded.support.TempDirSupplier
import java.io.File
import java.util.Properties
import java.util.concurrent.TimeUnit
import java.util.function.Supplier
import kotlin.concurrent.thread


class EmbeddedZookeeper(
        portSupplier: Supplier<Int> = RandomPortSupplier,
        private val dataLogDirSupplier: Supplier<File> = TempDirSupplier("zookeeper-log"),
        private val dataDirSupplier: Supplier<File> = TempDirSupplier("zookeeper-data"))
    : AbstractService(), AutoCloseable {

    private val logger = LoggerFactory.getLogger(javaClass)

    val port = portSupplier.get()

    private var zkServer: EmbeddedZooKeeperServer? = null
    private var cnxnFactory: ServerCnxnFactory? = null
    private var containerManager: ContainerManager? = null

    val zkConnect: String
        get() = "localhost:$port"


    override fun doStart() {

        val properties = Properties().apply {
            setProperty("clientPort", port.toString())
            setProperty("dataLogDir", dataLogDirSupplier.get().canonicalPath)
            setProperty("dataDir", dataDirSupplier.get().canonicalPath)
        }

        val quorumPeerConfig = QuorumPeerConfig()
                .apply { parseProperties(properties) }

        val serverConfig = ServerConfig()
                .apply { readFrom(quorumPeerConfig) }

        val zkServer = EmbeddedZooKeeperServer(serverConfig)
                .also { this.zkServer = it }

        ServerCnxnFactory.createFactory()
                .apply {
                    configure(serverConfig.clientPortAddress, serverConfig.maxClientCnxns)
                }
                .also { this.cnxnFactory = it }
                .startup(zkServer)

        ContainerManager(zkServer.zkDatabase, zkServer.firstProcessor,
                Integer.getInteger("znode.container.checkIntervalMs", TimeUnit.MINUTES.toMillis(1L).toInt()),
                Integer.getInteger("znode.container.maxPerMinute", 10000))
                .also { this.containerManager = it }
                .start()

        notifyStarted()
    }


    override fun doStop() {
        try {
            cnxnFactory?.run {
                shutdown()
                thread {
                    join()
                    notifyStopped()
                }
            }
        } catch (ex: Exception) {
            logger.error("Error while shutting down ServerCnxnFactory", ex)
        } finally {
            cnxnFactory = null
        }

        containerManager?.stop()
        containerManager = null

        try {
            zkServer?.run {
                shutdown()
                zkDatabase?.close()
            }
        } catch (ex: Exception) {
            logger.error("Error while shutting down ZooKeeperServer", ex)
        }
    }


    override fun close() {
        stopAsync()
                .awaitTerminated()
        (dataLogDirSupplier as? AutoCloseable)?.close()
        (dataDirSupplier as? AutoCloseable)?.close()
    }


    private class EmbeddedZooKeeperServer(config: ServerConfig)
        : ZooKeeperServer(config.dataLogDir, config.dataDir, config.tickTime) {

        init {
            setMinSessionTimeout(config.minSessionTimeout)
            setMaxSessionTimeout(config.maxSessionTimeout)
        }

        val firstProcessor: RequestProcessor? = super.firstProcessor
    }
}
