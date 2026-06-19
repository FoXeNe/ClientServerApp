package manager

import java.io.IOException
import java.net.Socket

class ConnectionManager(
    private val host: String,
    private val port: Int,
    private val maxAttempts: Int = 10,
    private val delay: Long = 3000L,
) {
    fun connect(): Socket {
        var socket: Socket? = null
        var attempt = 0
        var lastException: Exception? = null

        while (socket == null && attempt < maxAttempts) {
            try {
                attempt++
                socket = Socket(host, port)
            } catch (e: Exception) {
                lastException = e
                if (attempt < maxAttempts) Thread.sleep(delay)
            }
        }
        return socket ?: throw IOException("Cannot connect to $host:$port after $maxAttempts attempts", lastException)
    }
}
