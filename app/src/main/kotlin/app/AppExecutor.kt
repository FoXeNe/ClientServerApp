package app

import app.AppInitializer
import io.ConsoleHandler
import manager.CommandManager

class AppExecutor {
    var interactiveMode = true

    fun exec() {
        val io = ConsoleHandler()
        val manager = CommandManager()

        AppInitializer().setup(manager, io, this)

        io.println("введите help для получения информации о командах")

        while (interactiveMode) {
            val input = readln()
            manager.initCommand(input, io)
        }
    }

    fun stop() {
        interactiveMode = false
    }
}
