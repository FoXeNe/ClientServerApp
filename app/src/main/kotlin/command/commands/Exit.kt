package command.commands

import app.AppExecutor
import command.Command
import io.IOHandler

class Exit(
    private val io: IOHandler,
    private val stop: () -> Unit,
) : Command {
    override val name = "exit"
    override val description = "stop app execution"

    override fun execute() {
        io.println("завершение процесса")
        stop()
    }
}
