package command.commands

import command.Command
import io.IOHandler
import manager.CollectionManager
import reader.ProductReader

class Add(
    private val io: IOHandler,
    private val manager: CollectionManager,
) : Command {
    override val name = "add"
    override val description = "add product"

    override fun execute() {
        io.println("добавляем продукт")
        manager.addProduct(ProductReader(io).read())
    }
}
