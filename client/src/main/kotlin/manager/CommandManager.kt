package manager

import command.Command
import io.IOHandler
import java.util.LinkedList

class CommandManager {
    private val commands = mutableMapOf<String, Command>()
    private val history = LinkedList<Command>()

    fun register(command: Command) {
        commands[command.name] = command
    }

    fun initCommand(
        input: String,
        io: IOHandler,
    ) {
        val resInput = input.trim().split(" ")
        val name = resInput[0]
        val args = if (resInput.size > 1) resInput[1] else ""

        val command = commands[name]

        if (command != null) {
            command.execute(args)
            history.add(command)
        } else {
            io.println("команда не найдена")
        }
    }

    fun getCommands() = commands

    fun getHistory() = history
}
