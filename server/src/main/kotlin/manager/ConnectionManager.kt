package manager

import java.net.InetSocketAddress
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel

class ConnectionManager(
    private val addr: String,
    private val port: Int,
) {
    private val selector = Selector.open()
    private val socket = ServerSocketChannel.open()

    init {
        socket.configureBlocking(false)
        socket.bind(InetSocketAddress(addr, port))
    }

    fun exec() {
        socket.register(selector, SelectionKey.OP_ACCEPT)
        while (true) {
            println("waiting...")
            selector.select()

            val keys = selector.selectedKeys().iterator()

            while (keys.hasNext()) {
                val myKey = keys.next()
                keys.remove()
                when {
                    myKey.isAcceptable() -> {
                        val client = socket.accept()
                        client.configureBlocking(false)
                        client.register(selector, SelectionKey.OP_READ)
                        println("client connected")
                    }

                    myKey.isReadable() -> {
                        val client = myKey.channel() as SocketChannel
                        val buffer = ByteBuffer.allocate(1024)
                        val bytes =
                            try {
                                client.read(buffer)
                            } catch (e: Exception) {
                                -1
                            }
                        if (bytes == -1) {
                            println("client is down")
                            myKey.cancel()
                            client.close()
                        } else {
                            println("get bytes: $bytes")
                        }
                    }
                }
            }
        }
    }
}
