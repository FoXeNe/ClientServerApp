package command.commands

import command.Command
import io.IOHandler
import manager.CollectionManager
import manager.DatabaseManager
import model.CommandResult
import model.Product
import reader.ProductReader

class Update(
    private val io: IOHandler,
    private val db: DatabaseManager,
    private val collectionManager: CollectionManager,
) : Command {
    override val name = "update"
    override val description = "update element by id"

    override fun execute(
        args: String,
        product: Product?,
        ownerLogin: String?,
    ): CommandResult {
        val owner = ownerLogin ?: return CommandResult(false, "требуется авторизация")
        val id = args.trim().toLongOrNull() ?: return CommandResult(false, "введите id, например: update 5")
        val old = collectionManager.getById(id) ?: return CommandResult(false, "элемент с id=$id не найден")
        if (collectionManager.getOwner(id) != owner) return CommandResult(false, "нельзя обновить чужой элемент")
        val p = product ?: ProductReader(io).read()
        val updated = p.copy(
            id = old.id,
            creationDate = old.creationDate,
            manufacturer = p.manufacturer.copy(id = old.manufacturer.id),
        )
        if (!db.updateProduct(updated, owner)) return CommandResult(false, "не удалось обновить элемент")
        collectionManager.updateById(id, updated, owner)
        return CommandResult(true, "элемент обновлён")
    }
}
