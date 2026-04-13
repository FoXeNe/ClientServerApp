package app

import io.ConsoleHandler
import io.IOWrapper
import manager.CommandManager

class AppExecutor {
    var interactiveMode = true

    fun exec() {
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

    fun stop() {
        interactiveMode = false
    }
}
