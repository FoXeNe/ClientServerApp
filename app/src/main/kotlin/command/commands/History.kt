package command.commands

import command.Command
import io.IOHandler
import manager.CommandManager

class History(
    private val io: IOHandler,
    private val commandManager: CommandManager,
) : Command {
    override val name = "history"
    override val description = "last 10 command was written"

    override fun execute(args: String) {
        for (command in commandManager.getHistory()) {
            io.println(command.name)
        }
    }
}
