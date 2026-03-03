package command.commands

import command.Command
import command.CommandManager
import io.IOHandler

class Help(
    private val io: IOHandler,
    private val commandManager: CommandManager,
) : Command {
    override val name = "help"
    override val description = "show avaliable commands"

    override fun execute() {
        for (command in commandManager.getCommands().values) {
            io.println("${command.name}: ${command.description}")
        }
    }
}
