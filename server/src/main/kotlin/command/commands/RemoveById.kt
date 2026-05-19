package command.commands

import command.Command
import manager.CollectionManager
import manager.ProductRepository
import model.CommandResult
import model.Product

class RemoveById(
    private val db: ProductRepository,
    private val collectionManager: CollectionManager,
) : Command {
    override val name = "remove_by_id"
    override val description = "remove element by id"

    override fun execute(
        args: String,
        product: Product?,
        ownerLogin: String?,
    ): CommandResult {
        val owner = ownerLogin ?: return CommandResult(false, "требуется авторизация")
        val id = args.trim().toLongOrNull() ?: return CommandResult(false, "введите id, например: remove_by_id 5")
        if (!collectionManager.hasId(id)) return CommandResult(false, "элемент с id=$id не найден")
        if (collectionManager.getOwner(id) != owner) return CommandResult(false, "нельзя удалить чужой элемент")
        if (!db.delete(id, owner)) return CommandResult(false, "не удалось удалить элемент")
        collectionManager.removeById(id, owner)
        return CommandResult(true, "элемент удалён")
    }
}
