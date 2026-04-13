package command.commands

import command.Command
import io.IOHandler

class Exit(
    private val io: IOHandler,
    private val stop: () -> Unit,
) : Command {
    override val name = "exit"
    override val description = "stop app execution"

    override fun execute(args: String) {
        io.println("завершение процесса")
        stop()
    }
}
