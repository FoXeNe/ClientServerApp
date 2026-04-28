package command.commands

import command.Command
import io.IOHandler
import manager.CollectionManager
import model.CommandResult
import model.Product
import reader.ProductReader

class AddIfMin(
    private val io: IOHandler,
    private val collectionManager: CollectionManager,
) : Command {
    override val name = "add_if_min"
    override val description = "add an object if its value is less than the minimum value in the collection"

    override fun execute(
        args: String,
        product: Product?,
    ): CommandResult {
        val p = product ?: ProductReader(io).read()
        val min = collectionManager.getMinProduct()
        return if (min == null || p < min) {
            collectionManager.addProduct(p)
            CommandResult(true, "продукт добавлен")
        } else {
            CommandResult(true, "цена не меньше минимальной")
        }
    }
}
