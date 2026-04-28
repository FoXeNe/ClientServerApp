package command.commands

import command.Command
import manager.CollectionManager
import model.CommandResult
import model.Product

class Clear(
    private val collectionManager: CollectionManager,
) : Command {
    override val name = "clear"
    override val description = "clear colletion"

    override fun execute(
        args: String,
        product: Product?,
    ): CommandResult {
        collectionManager.clear()
        return CommandResult(true, "коллекция очищена")
    }
}
