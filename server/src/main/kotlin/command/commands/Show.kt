package command.commands

import command.Command
import manager.CollectionManager
import model.CommandResult
import model.Product

class Show(
    private val collectionManager: CollectionManager,
) : Command {
    override val name = "show"
    override val description = "show collection elements"

    override fun execute(
        args: String,
        product: Product?,
    ): CommandResult {
        val sorted = collectionManager.getCollection().sorted()
        val msg =
            if (sorted.isEmpty()) {
                "коллекция пустая"
            } else {
                "элементов: ${sorted.size}"
            }
        return CommandResult(true, msg, sorted)
    }
}
