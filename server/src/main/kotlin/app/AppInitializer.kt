package app

import command.commands.*
import io.IOWrapper
import manager.CollectionManager
import manager.CommandManager
import manager.FileManager
import manager.JsonManager
import manager.RequestHandler
import manager.WalManager
import model.Product
import java.io.File
import java.util.LinkedList
import java.util.logging.Logger

const val ENV_FILE = "COLLECTION_FILE"

class AppInitializer {
    private val logger = Logger.getLogger(AppInitializer::class.java.name)

    fun setup(
        commandManager: CommandManager,
        io: IOWrapper,
        app: AppExecutor,
    ): RequestHandler {
        val filePath = System.getenv(ENV_FILE)
        val walPath = createWalPath(filePath)
        val walManager = WalManager(walPath)
        val fileManager = FileManager(filePath, io)
        var baseCollection: LinkedList<Product>
        if (filePath == null) {
            logger.info("путь к файлу не задан")
            io.println("коллекция не загружена")
            baseCollection = LinkedList()
        } else {
            try {
                baseCollection = JsonManager(filePath).readCollection()
                logger.info("коллекция загружена из $filePath")
                io.println("коллекция загружена")
            } catch (e: Exception) {
                logger.warning("не удалось загрузить коллекцию: ${e.message}")
                io.println("не удалось загрузить коллекцию из файла: ${e.message}")
                baseCollection = LinkedList()
            }
        }

        val collectionManager = CollectionManager(io, baseCollection, walManager)

        if (walManager.hasEntries()) {
            val entries = walManager.readAll()
            for (entry in entries) {
                collectionManager.replayEntry(entry)
            }
            logger.info("операции восстановлены из журнала")
            io.println("операции восстановлены из журнала")
        }

        commandManager.register(Add(io, collectionManager))
        commandManager.register(AddIfMin(io, collectionManager))
        commandManager.register(Clear(collectionManager))
        commandManager.register(FilterByManufacturer(collectionManager))
        commandManager.register(FilterGreaterThanManufacturer(collectionManager))
        commandManager.register(Info(collectionManager))
        commandManager.register(RemoveById(collectionManager))
        commandManager.register(RemoveFirst(collectionManager))
        commandManager.register(Show(collectionManager))
        commandManager.register(SumOfPrice(collectionManager))
        commandManager.register(Update(io, collectionManager))
        commandManager.register(Save(collectionManager, fileManager, walManager))

        return RequestHandler(commandManager)
    }

    private fun createWalPath(mainFilePath: String?): String {
        if (mainFilePath != null) {
            return "$mainFilePath.wal"
        }
        val tmpDir = System.getProperty("java.io.tmpdir")
        return File(tmpDir, "collection_session.wal").absolutePath
    }
}
