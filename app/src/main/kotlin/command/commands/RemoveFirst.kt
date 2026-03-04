package command.commands

import command.Command
import io.IOHandler
import manager.CollectionManager
import reader.ProductReader

class RemoveFirst(
    private val io: IOHandler,
    private val collectionManager: CollectionManager,
) : Command {
    override val name = "remove_first"
    override val description = "remove first element"

    override fun execute(args: String) {
        if (collectionManager.getCollection().isNotEmpty()) {
            collectionManager.removeFirst()
        } else {
            io.println("коллекция пустая, невозможно удалить первый элемент")
        }
    }
}
