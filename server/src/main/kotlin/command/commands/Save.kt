package command.commands

import command.Command
import manager.CollectionManager
import manager.FileManager
import manager.WalManager
import model.CommandResult
import model.Product

class Save(
    private val collectionManager: CollectionManager,
    private val fileManager: FileManager,
    private val walManager: WalManager,
) : Command {
    override val name = "save"
    override val description = "save collection to the file"

    override fun execute(
        args: String,
        product: Product?,
    ): CommandResult {
        val collection = collectionManager.getCollection()

        if (fileManager.isUsingTempFile() && fileManager.isMainValid()) {
            val merged = fileManager.restoreMainFile(collection)
            if (merged != null) {
                collectionManager.replaceCollection(merged)
                walManager.clear()
                return CommandResult(true, "данные восстановлены в основной файл")
            }
        }

        return if (fileManager.tryWrite(collection)) {
            if (!fileManager.isUsingTempFile()) {
                walManager.clear()
                CommandResult(true, "коллекция сохранена")
            } else {
                CommandResult(true, "коллекция сохранена во временный файл")
            }
        } else {
            if (!fileManager.isUsingTempFile()) {
                fileManager.offerTempFile(collection)
                CommandResult(false, "не удалось сохранить в основной файл")
            } else {
                CommandResult(false, "временный файл недоступен, данные не сохранены")
            }
        }
    }
}
