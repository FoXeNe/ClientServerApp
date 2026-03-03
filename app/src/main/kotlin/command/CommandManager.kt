package command

import io.IOHandler

class CommandManager {
    private val commands = mutableMapOf<String, Command>()

    fun register(command: Command) {
        commands[command.name] = command
    }

    fun initCommand(
        input: String,
        io: IOHandler,
    ) {
        val command = commands[input]

        if (command != null) {
            command?.execute()
        } else {
            io.println("команда не найдена")
        }
    }

    fun getCommands() = commands
}
