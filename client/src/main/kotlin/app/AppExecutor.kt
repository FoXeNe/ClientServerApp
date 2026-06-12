package app

import io.ConsoleHandler
import io.IOWrapper
import manager.CommandManager
import manager.NetworkManager

class AppExecutor {
    var interactiveMode = true
    private var exitCode = 0

    fun exec() {
        val io = IOWrapper(ConsoleHandler())
        val manager = CommandManager()
        val host = System.getenv("SERVER_HOST") ?: "localhost"
        val port = System.getenv("SERVER_PORT")?.toIntOrNull() ?: 45205
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
        exitCode = 1
    }

    fun getExitCode() = exitCode
}
