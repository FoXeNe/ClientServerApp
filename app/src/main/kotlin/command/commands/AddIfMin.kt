package command.commands

import command.Command
import io.IOHandler
import manager.CollectionManager
import reader.ProductReader

class AddIfMin(
    private val io: IOHandler,
    private val collectionManager: CollectionManager,
) : Command {
    override val name = "add_if_min"
    override val description = "add an object if its value is less than the minimum value in the collection"

    override fun execute(args: String) {
        val newProduct = ProductReader(io).read()
        val min = collectionManager.getMinProduct()
        if (min == null || newProduct < min) {
            collectionManager.addProduct(newProduct)
        } else {
            io.println("цена не меньше минимальной")
        }
    }
}
