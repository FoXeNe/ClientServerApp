package command.commands

import command.Command
import io.IOHandler
import manager.CollectionManager
import reader.ProductReader

class Add(
    private val io: IOHandler,
    private val collectionManager: CollectionManager,
) : Command {
    override val name = "add"
    override val description = "add product"

    override fun execute() {
        collectionManager.addProduct(ProductReader(io).read())
    }
}
