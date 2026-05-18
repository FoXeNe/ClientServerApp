package manager

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

    init {
        serverChannel.configureBlocking(true)
        serverChannel.bind(InetSocketAddress(addr, port))
        logger.info("сервер слушает $addr:$port")
    }

    fun exec() {
        while (!serverChannel.socket().isClosed) {
            try {
                val client = serverChannel.accept() ?: continue
                logger.info("новое подключение: ${client.remoteAddress}")
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
        serverChannel.close()
        readPool.shutdown()
        processPool.shutdown()
        if (!readPool.awaitTermination(5, TimeUnit.SECONDS)) readPool.shutdownNow()
        if (!processPool.awaitTermination(5, TimeUnit.SECONDS)) processPool.shutdownNow()
    }

    private fun handleRead(channel: SocketChannel) {
        try {
            val msgBytes =
                readCompleteMessage(channel) ?: run {
                    logger.info("клиент отключился: ${channel.remoteAddress}")
                    channel.close()
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
                    logger.warning("ошибка: ${e.message}")
                    runCatching { channel.close() }
                }
            }
        } catch (e: Exception) {
            logger.warning("ошибка: ${e.message}")
            runCatching { channel.close() }
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
