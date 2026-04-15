package manager

import java.net.Socket
import kotlin.system.exitProcess

class ConnectionManager(
    private val host: String,
    private val port: Int,
    private val maxAttempts: Int = 10,
    private val delay: Long = 3000L,
) {
    fun connect(): Socket {
        var socket: Socket? = null
        var attempt = 0

        println("waiting for the server")

        while (socket == null) {
            try {
                attempt++
                socket = Socket(host, port)
                println("connected to the server")
            } catch (e: Exception) {
                if (attempt >= maxAttempts) {
                    println("max attempts reached")
                    exitProcess(1)
                }

                println("server is not reacheable, waiting...")
                Thread.sleep(delay)
            }
        }
        return socket
    }
}
