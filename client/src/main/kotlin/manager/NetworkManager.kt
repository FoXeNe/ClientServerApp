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
    private var socket: Socket = ConnectionManager(host, port).connect()
    private var out = DataOutputStream(socket.getOutputStream())
    private var input = DataInputStream(socket.getInputStream())

    fun sendRequest(request: Request): Response? =
        try {
            send(request)
        } catch (e: IOException) {
            reconnectAndSend(request)
        }

    private fun reconnectAndSend(request: Request): Response? {
        try {
            socket.close()
        } catch (e: Exception) {
        }
        return try {
            socket = ConnectionManager(host, port).connect()
            out = DataOutputStream(socket.getOutputStream())
            input = DataInputStream(socket.getInputStream())
            send(request)
        } catch (e: Exception) {
            null
        }
    }

    private fun send(request: Request): Response {
        val byteOut = ByteArrayOutputStream()
        val objOut = ObjectOutputStream(byteOut)
        objOut.writeObject(request)
        objOut.close()

        val data = byteOut.toByteArray()
        out.writeInt(data.size)
        out.write(data)
        out.flush()

        val length = input.readInt()
        val responseBytes = ByteArray(length)
        input.readFully(responseBytes)

        val byteIn = ByteArrayInputStream(responseBytes)
        val objIn = ObjectInputStream(byteIn)
        val response = objIn.readObject() as Response
        objIn.close()
        return response
    }
}
