package manager

import model.Request
import model.Response
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.util.logging.Logger

class ConnectionManager(
    private val addr: String,
    private val port: Int,
    private val requestHandler: RequestHandler,
) {
    private val logger = Logger.getLogger(ConnectionManager::class.java.name)
    private val selector = Selector.open()
    private val serverChannel = ServerSocketChannel.open()

    init {
        serverChannel.configureBlocking(false)
        serverChannel.bind(InetSocketAddress(addr, port))
        serverChannel.register(selector, SelectionKey.OP_ACCEPT)
        logger.info("сервер слушает $addr:$port")
    }

    fun exec() {
        selector.select(50)

        val keys = selector.selectedKeys().iterator()
        while (keys.hasNext()) {
            val key = keys.next()
            keys.remove()
            when {
                key.isAcceptable -> acceptClient()
                key.isReadable -> readClient(key)
            }
        }
    }

    private fun acceptClient() {
        val client = serverChannel.accept()
        if (client == null) return
        client.configureBlocking(false)
        client.register(selector, SelectionKey.OP_READ, ClientBuffer())
        logger.info("новое подключение: ${client.remoteAddress}")
    }

    private fun readClient(key: SelectionKey) {
        val client = key.channel() as SocketChannel
        val clientBuffer = key.attachment() as ClientBuffer
        val buffer = ByteBuffer.allocate(4096)

        val bytesRead: Int
        try {
            bytesRead = client.read(buffer)
        } catch (e: Exception) {
            logger.warning("ошибка чтения ${client.remoteAddress}: ${e.message}")
            key.cancel()
            client.close()
            return
        }

        if (bytesRead == -1) {
            logger.info("клиент отключился: ${client.remoteAddress}")
            key.cancel()
            client.close()
            return
        }

        if (bytesRead > 0) {
            buffer.flip()
            clientBuffer.append(buffer.array(), buffer.limit())
            logger.fine("получено байт: $bytesRead от ${client.remoteAddress}")
        }

        val msgBytes = clientBuffer.readMessage()
        if (msgBytes == null) return

        logger.info("получен запрос от ${client.remoteAddress}")
        val request = deserialize(msgBytes)
        val response = requestHandler.handle(request)
        logger.info("отправка ответа клиенту ${client.remoteAddress}")
        sendResponse(client, response)
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
        while (buf.hasRemaining()) {
            channel.write(buf)
        }
    }

    private fun serialize(response: Response): ByteArray {
        val baos = ByteArrayOutputStream()
        val oos = ObjectOutputStream(baos)
        oos.writeObject(response)
        oos.close()
        return baos.toByteArray()
    }

    private fun deserialize(bytes: ByteArray): Request {
        val bais = ByteArrayInputStream(bytes)
        val ois = ObjectInputStream(bais)
        val obj = ois.readObject() as Request
        ois.close()
        return obj
    }
}
