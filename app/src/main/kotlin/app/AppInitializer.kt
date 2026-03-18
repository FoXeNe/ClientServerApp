package app

import command.commands.*
import io.IOWrapper
import manager.CollectionManager
import manager.CommandManager
import manager.FileManager
import manager.JsonManager
import manager.WalManager
import java.io.File
import java.util.LinkedList

const val ENV_FILE = "COLLECTION_FILE"

class AppInitializer {
    fun setup(
        commandManager: CommandManager,
        io: IOWrapper,
        app: AppExecutor,
    ) {
        val filePath = System.getenv(ENV_FILE)

        val walPath = createWalPath(filePath)
        val walManager = WalManager(walPath)

        val fileManager = FileManager(filePath, io)

        val baseCollection =
            if (filePath != null) {
                try {
                    val loaded = JsonManager(filePath).readCollection()
                    io.println("коллекция загружена")
                    loaded
                } catch (e: Exception) {
                    io.println("не удалось загрузить коллекцию из файла: ${e.message}")
                    LinkedList()
                }
            } else {
                io.println("коллекция не загружена")
                LinkedList()
            }

        val collectionManager = CollectionManager(io, baseCollection, walManager)

        if (walManager.hasEntries()) {
            val entries = walManager.readAll()
            for (entry in entries) {
                collectionManager.replayEntry(entry)
            }
            io.println("операции восстановлены из журнала")
        }

        commandManager.register(Add(io, collectionManager))
        commandManager.register(Clear(io, collectionManager))
        commandManager.register(Help(io, commandManager))
        commandManager.register(History(io, commandManager))
        commandManager.register(Info(io, collectionManager))
        commandManager.register(RemoveById(io, collectionManager))
        commandManager.register(RemoveFirst(io, collectionManager))
        commandManager.register(Update(io, collectionManager))
        commandManager.register(Exit(io, { app.stop() }))
        commandManager.register(ExecuteScript(io, commandManager))
        commandManager.register(Show(io, collectionManager))
        commandManager.register(Save(io, collectionManager, fileManager, walManager))
        commandManager.register(SumOfPrice(io, collectionManager))
        commandManager.register(FilterByManufacturer(io, collectionManager))
        commandManager.register(FilterGreaterThanManufacturer(io, collectionManager))
        commandManager.register(AddIfMin(io, collectionManager))
    }

    private fun createWalPath(mainFilePath: String?): String =
        if (mainFilePath != null) {
            "$mainFilePath.wal"
        } else {
            val tmpDir = System.getProperty("java.io.tmpdir")
            File(tmpDir, "collection_session.wal").absolutePath
        }
}
