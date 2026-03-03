package command.commands

import command.Command
import io.IOHandler
import manager.CollectionManager

class Show(
    private val io: IOHandler,
    private val collectionManager: CollectionManager,
) : Command {
    override val name = "show"
    override val description = "show collection elements"

    override fun execute() {
        for (product in collectionManager.getCollection()) {
            io.println(product.toString())
        }
    }
}
