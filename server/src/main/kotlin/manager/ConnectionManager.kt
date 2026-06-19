package manager

import com.sun.net.httpserver.HttpServer as MetricsHttpServer
import model.Request
import model.Response
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.ClosedChannelException
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Logger

class ConnectionManager(
    private val addr: String,
    private val port: Int,
    private val requestHandler: RequestHandler,
) : AutoCloseable {
    private val logger = Logger.getLogger(ConnectionManager::class.java.name)
    private val serverChannel = ServerSocketChannel.open()
    private val readPool = Executors.newFixedThreadPool(4)
    private val processPool = Executors.newFixedThreadPool(4)
    private val activeConnections = AtomicInteger(0)
    private val metricsServer: MetricsHttpServer

    init {
        serverChannel.configureBlocking(true)
        serverChannel.bind(InetSocketAddress(addr, port))
        logger.info("сервер слушает $addr:$port")

        val metricsPort = System.getenv("METRICS_PORT")?.toIntOrNull() ?: 8081
        val podName = System.getenv("HOSTNAME") ?: "unknown"
        metricsServer = MetricsHttpServer.create(InetSocketAddress(metricsPort), 0)
        metricsServer.createContext("/connections") { exchange ->
            val body = """{"connections":${activeConnections.get()},"pod":"$podName"}""".toByteArray()
            exchange.responseHeaders.set("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
        metricsServer.start()
        logger.info("метрики на порту $metricsPort /connections")
    }

    fun exec() {
        while (!serverChannel.socket().isClosed) {
            try {
                val client = serverChannel.accept() ?: continue
                activeConnections.incrementAndGet()
                logger.info("новое подключение: ${client.remoteAddress} (активных: ${activeConnections.get()})")
                readPool.submit { handleRead(client) }
            } catch (e: ClosedChannelException) {
                break
            } catch (e: Exception) {
                logger.warning("ошибка accept: ${e.message}")
            }
        }
    }

    override fun close() {
        logger.info("завершение работы ConnectionManager")
        metricsServer.stop(0)
        serverChannel.close()
        readPool.shutdown()
        processPool.shutdown()
        if (!readPool.awaitTermination(5, TimeUnit.SECONDS)) readPool.shutdownNow()
        if (!processPool.awaitTermination(5, TimeUnit.SECONDS)) processPool.shutdownNow()
    }

    private fun disconnect(channel: SocketChannel) {
        activeConnections.decrementAndGet()
        logger.info("клиент отключился: ${channel.remoteAddress} (активных: ${activeConnections.get()})")
        runCatching { channel.close() }
    }

    private fun handleRead(channel: SocketChannel) {
        try {
            val msgBytes = readCompleteMessage(channel) ?: run {
                disconnect(channel)
                return
            }
            logger.info("получен запрос от ${channel.remoteAddress}")
            processPool.submit {
                try {
                    val request = deserialize(msgBytes)
                    val response = requestHandler.handle(request)
                    sendResponse(channel, response)
                    readPool.submit { handleRead(channel) }
                } catch (e: Exception) {
                    logger.warning("ошибка обработки: ${e.message}")
                    disconnect(channel)
                }
            }
        } catch (e: Exception) {
            logger.warning("ошибка чтения: ${e.message}")
            disconnect(channel)
        }
    }

    private fun readCompleteMessage(channel: SocketChannel): ByteArray? {
        val lenBuf = ByteBuffer.allocate(4)
        while (lenBuf.hasRemaining()) {
            val n = channel.read(lenBuf)
            if (n == -1) return null
        }
        lenBuf.flip()
        val length = lenBuf.int

        val msgBuf = ByteBuffer.allocate(length)
        while (msgBuf.hasRemaining()) {
            val n = channel.read(msgBuf)
            if (n == -1) return null
        }
        return msgBuf.array()
    }

    private fun sendResponse(
        channel: SocketChannel,
        response: Response,
    ) {
        val bytes = serialize(response)
        val buf = ByteBuffer.allocate(4 + bytes.size)
        buf.putInt(bytes.size)
        buf.put(bytes)
        buf.flip()
        synchronized(channel) {
            while (buf.hasRemaining()) {
                channel.write(buf)
            }
        }
    }

    private fun serialize(response: Response): ByteArray {
        val baos = ByteArrayOutputStream()
        ObjectOutputStream(baos).use { it.writeObject(response) }
        return baos.toByteArray()
    }

    private fun deserialize(bytes: ByteArray): Request {
        val bais = ByteArrayInputStream(bytes)
        return ObjectInputStream(bais).use { it.readObject() as Request }
    }
}
