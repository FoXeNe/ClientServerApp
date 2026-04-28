package command.commands

import command.Command
import io.IOHandler
import manager.CollectionManager
import model.CommandResult
import model.Product
import reader.ProductReader

class Update(
    private val io: IOHandler,
    private val collectionManager: CollectionManager,
) : Command {
    override val name = "update"
    override val description = "update element by id"

    override fun execute(
        args: String,
        product: Product?,
    ): CommandResult {
        val id =
            args.trim().toLongOrNull()
                ?: return CommandResult(false, "введите id, к примеру: update 5")
        if (collectionManager.getCollection().none { it.id == id }) {
            return CommandResult(false, "элемент с id=$id не найден")
        }
        val p = product ?: ProductReader(io).read()
        collectionManager.updateById(id, p)
        return CommandResult(true, "элемент обновлён")
    }
}
