package command.commands

import command.Command
import manager.CollectionManager
import manager.ProductRepository
import model.CommandResult
import model.Product

class RemoveFirst(
    private val db: ProductRepository,
    private val collectionManager: CollectionManager,
) : Command {
    override val name = "remove_first"
    override val description = "remove first element"

    override fun execute(
        args: String,
        product: Product?,
        ownerLogin: String?,
    ): CommandResult {
        val owner = ownerLogin ?: return CommandResult(false, "требуется авторизация")
        val first = collectionManager.getFirst() ?: return CommandResult(false, "коллекция пустая")
        if (collectionManager.getOwner(first.id) != owner) return CommandResult(false, "первый элемент принадлежит другому пользователю")
        if (!db.delete(first.id, owner)) return CommandResult(false, "не удалось удалить первый элемент")
        collectionManager.removeById(first.id, owner)
        return CommandResult(true, "первый элемент удалён")
    }
}
