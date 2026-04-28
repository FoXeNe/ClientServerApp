package command.commands

import command.Command
import manager.CollectionManager
import model.CommandResult
import model.Product

class Info(
    private val collectionManager: CollectionManager,
) : Command {
    override val name = "info"
    override val description = "show collection info"

    override fun execute(
        args: String,
        product: Product?,
    ): CommandResult = CommandResult(true, collectionManager.getInfoString())
}
