package app

import io.ConsoleHandler
import io.IOWrapper
import manager.CommandManager
import manager.NetworkManager

class AppExecutor {
    var interactiveMode = true

    fun exec() {
        val io = IOWrapper(ConsoleHandler())
        val manager = CommandManager()
        val host = System.getenv("SERVER_HOST") ?: "localhost"
        val port = System.getenv("SERVER_PORT")?.toIntOrNull() ?: 9090
        val network = NetworkManager(host, port)

        AppInitializer().setup(manager, io, this, network)

        io.println("введите help для получения информации о командах")

        while (interactiveMode) {
            val input = io.readLine() ?: break
            if (input.isNotBlank()) {
                val result = manager.initCommand(input)
                io.println(result.message)
                result.collection?.forEach { io.println(it.toString()) }
            }
        }
    }

    fun stop() {
        interactiveMode = false
    }
}
