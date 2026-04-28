package command.commands

import command.Command
import manager.CollectionManager
import model.CommandResult
import model.Product

class RemoveFirst(
    private val collectionManager: CollectionManager,
) : Command {
    override val name = "remove_first"
    override val description = "remove first element"

    override fun execute(
        args: String,
        product: Product?,
    ): CommandResult {
        if (collectionManager.getCollection().isEmpty()) {
            return CommandResult(false, "коллекция пустая, невозможно удалить первый элемент")
        }
        collectionManager.removeFirst()
        return CommandResult(true, "первый элемент удалён")
    }
}
