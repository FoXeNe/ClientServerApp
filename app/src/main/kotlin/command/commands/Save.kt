package command.commands

import command.Command
import io.IOHandler
import manager.CollectionManager
import manager.FileManager
import manager.WalManager

class Save(
    private val io: IOHandler,
    private val collectionManager: CollectionManager,
    private val fileManager: FileManager,
    private val walManager: WalManager,
) : Command {
    override val name = "save"
    override val description = "save collection to the file"

    override fun execute(args: String) {
        val collection = collectionManager.getCollection()

        if (fileManager.isUsingTempFile() && fileManager.isMainValid()) {
            val merged = fileManager.restoreMainFile(collection)
            if (merged != null) {
                collectionManager.replaceCollection(merged)
                walManager.clear()
                return
            }
        }

        if (fileManager.tryWrite(collection)) {
            if (!fileManager.isUsingTempFile()) {
                walManager.clear()
                io.println("коллекция сохранена")
            } else {
                io.println("коллекция сохранена во временный файл")
            }
        } else {
            if (!fileManager.isUsingTempFile()) {
                fileManager.offerTempFile(collection)
            } else {
                io.println("временный файл недоступен, данные не сохранены")
            }
        }
    }
}
