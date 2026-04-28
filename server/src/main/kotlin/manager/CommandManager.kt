package manager

import command.Command
import model.CommandResult
import java.util.LinkedList

class CommandManager {
    private val commands = mutableMapOf<String, Command>()
    private val history = LinkedList<Command>()

    fun register(command: Command) {
        commands[command.name] = command
    }

    fun initCommand(input: String): CommandResult {
        val resInput = input.trim().split(" ")
        val name = resInput[0]
        val args = if (resInput.size > 1) resInput[1] else ""

        val command =
            commands[name]
                ?: return CommandResult(false, "команда не найдена")

        history.add(command)
        return command.execute(args)
    }

    fun getCommands() = commands

    fun getHistory() = history
}
