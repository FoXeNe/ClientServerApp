package command

import model.CommandResult

interface Command {
    val name: String
    val description: String

    fun execute(args: String): CommandResult
}
