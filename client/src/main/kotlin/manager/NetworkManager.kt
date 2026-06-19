package manager

import model.Request
import model.Response
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket

class NetworkManager(
    private val host: String,
    private val port: Int,
) {
    private var socket: Socket? = null
    private var out: DataOutputStream? = null
    private var input: DataInputStream? = null

    var login: String? = null
    var password: String? = null

    fun setCredentials(
        login: String,
        password: String,
    ) {
        this.login = login
        this.password = password
    }

    fun sendRequest(request: Request): Response? {
        val withCreds = request.copy(login = login, password = password)
        return trySend(withCreds)
    }

    fun sendRaw(request: Request): Response? = trySend(request)

    fun resetConnection() {
        synchronized(this) {
            runCatching { socket?.close() }
            socket = null
            out = null
            input = null
        }
    }

    private fun trySend(request: Request): Response? = synchronized(this) {
        ensureConnected()
        try {
            send(request)
        } catch (e: Exception) {
            reconnectAndSend(request)
        }
    }

    private fun ensureConnected() {
        if (socket == null || socket!!.isClosed) {
            val s = Socket(host, port)
            socket = s
            out = DataOutputStream(s.getOutputStream())
            input = DataInputStream(s.getInputStream())
        }
    }

    private fun reconnectAndSend(request: Request): Response? {
        runCatching { socket?.close() }
        socket = null
        return try {
            ensureConnected()
            send(request)
        } catch (e: Exception) {
            null
        }
    }

    private fun send(request: Request): Response {
        val byteOut = ByteArrayOutputStream()
        ObjectOutputStream(byteOut).use { it.writeObject(request) }

        val data = byteOut.toByteArray()
        out!!.writeInt(data.size)
        out!!.write(data)
        out!!.flush()

        val length = input!!.readInt()
        if (length <= 0) throw IOException("Invalid response length: $length")
        val responseBytes = ByteArray(length)
        input!!.readFully(responseBytes)

        return ObjectInputStream(ByteArrayInputStream(responseBytes)).use {
            it.readObject() as Response
        }
    }
}
