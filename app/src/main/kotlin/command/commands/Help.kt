package command.commands

import command.Command
import io.IOHandler
import manager.CommandManager

class Help(
    private val io: IOHandler,
    private val commandManager: CommandManager,
) : Command {
    override val name = "help"
    override val description = "show avaliable commands"

    override fun execute(args: String) {
        for (command in commandManager.getCommands().values) {
            io.println("${command.name}: ${command.description}")
        }
    }
}
