package command

import model.CommandResult
import model.Product

interface Command {
    val name: String
    val description: String

    fun execute(
        args: String,
        product: Product? = null,
    ): CommandResult
}
