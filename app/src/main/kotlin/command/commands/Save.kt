package command.commands

import command.Command
import io.IOHandler
import manager.CollectionManager

class Save(
    private val io: IOHandler,
    private val collectionManager: CollectionManager,
) : Command {
    override val name = "save"
    override val description = "save collection to the file"

    override fun execute(args: String) {
        collectionManager.saveToFile()
    }
}
