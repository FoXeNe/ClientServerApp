package command.commands

import command.Command
import manager.CollectionManager
import model.CommandResult
import model.Product

class RemoveById(
    private val collectionManager: CollectionManager,
) : Command {
    override val name = "remove_by_id"
    override val description = "remove element by id"

    override fun execute(
        args: String,
        product: Product?,
    ): CommandResult {
        val id =
            args.trim().toLongOrNull()
                ?: return CommandResult(false, "введите id, к примеру: remove_by_id 5")
        if (collectionManager.getCollection().none { it.id == id }) {
            return CommandResult(false, "элемент с id=$id не найден")
        }
        collectionManager.removeById(id)
        return CommandResult(true, "элемент удалён")
    }
}
