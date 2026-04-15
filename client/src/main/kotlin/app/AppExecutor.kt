package app

import io.ConsoleHandler
import io.IOWrapper
import manager.CommandManager
import manager.ConnectionManager
import java.net.Socket

class AppExecutor {
    var interactiveMode = true

    fun exec() {
        // connecting to the server
        val connectionManager = ConnectionManager("localhost", 9090)
        val socket = connectionManager.connect()
        connection(socket)

        // app logic
        val io = IOWrapper(ConsoleHandler())
        val manager = CommandManager()

        AppInitializer().setup(manager, io, this)

        io.println("введите help для получения информации о командах")

        while (interactiveMode) {
            val input = io.readLine() ?: break
            if (input.isNotBlank()) {
                manager.initCommand(input, io)
            }
        }
    }

    private fun connection(socket: Socket) {
        println("server connected")
        // TODO: add working with server
    }

    fun stop() {
        interactiveMode = false
    }
}
