package command.commands

import command.Command
import io.IOHandler
import manager.CollectionManager

class Clear(
    private val io: IOHandler,
    private val collectionManager: CollectionManager,
) : Command {
    override val name = "clear"
    override val description = "clear colletion"

    override fun execute() {
        collectionManager.getList().clear()
        io.println(collectionManager.getList().toString())
    }
}
