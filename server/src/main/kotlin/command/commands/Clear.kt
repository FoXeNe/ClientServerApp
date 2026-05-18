package command.commands

import command.Command
import manager.CollectionManager
import manager.DatabaseManager
import model.CommandResult
import model.Product

class Clear(
    private val db: DatabaseManager,
    private val collectionManager: CollectionManager,
) : Command {
    override val name = "clear"
    override val description = "clear your objects from collection"

    override fun execute(
        args: String,
        product: Product?,
        ownerLogin: String?,
    ): CommandResult {
        val owner = ownerLogin ?: return CommandResult(false, "требуется авторизация")
        val count = db.clearUserProducts(owner)
        collectionManager.clear(owner)
        return CommandResult(true, "удалено элементов: $count")
    }
}
