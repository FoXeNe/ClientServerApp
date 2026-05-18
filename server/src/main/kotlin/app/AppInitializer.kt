package app

import command.commands.*
import io.IOWrapper
import manager.CollectionManager
import manager.CommandManager
import manager.DatabaseManager
import manager.RequestHandler
import manager.UserManager
import java.util.logging.Logger

class AppInitializer {
    private val logger = Logger.getLogger(AppInitializer::class.java.name)

    private fun connectWithRetry(
        url: String,
        user: String,
        password: String,
        retries: Int = 10,
    ): DatabaseManager {
        repeat(retries) { attempt ->
            try {
                return DatabaseManager(url, user, password)
            } catch (e: Exception) {
                if (attempt == retries - 1) throw e
                logger.warning("БД недоступна, пробую еще раз: ${e.message}")
                Thread.sleep(3000)
            }
        }
        throw RuntimeException("не удалось подключиться к БД после $retries попыток")
    }

    fun setup(
        commandManager: CommandManager,
        io: IOWrapper,
    ): RequestHandler {
        val host = System.getenv("DB_HOST") ?: "pg"
        val port = System.getenv("DB_PORT") ?: "5432"
        val dbName = System.getenv("DB_NAME") ?: "studs"
        val dbUser = System.getenv("DB_USER") ?: System.getProperty("user.name") ?: "studs"
        val dbPassword = System.getenv("DB_PASSWORD") ?: ""
        val url = "jdbc:postgresql://$host:$port/$dbName"

        logger.info("подключение к бд")
        val db = connectWithRetry(url, dbUser, dbPassword)
        val userManager = UserManager(db)

        val initialData = db.loadAll()
        logger.info("загружено из БД: ${initialData.size} элементов")

        val collectionManager = CollectionManager(initialData)

        commandManager.register(Add(io, db, collectionManager))
        commandManager.register(AddIfMin(io, db, collectionManager))
        commandManager.register(Clear(db, collectionManager))
        commandManager.register(FilterByManufacturer(collectionManager))
        commandManager.register(FilterGreaterThanManufacturer(collectionManager))
        commandManager.register(Info(collectionManager))
        commandManager.register(Login())
        commandManager.register(Register())
        commandManager.register(RemoveById(db, collectionManager))
        commandManager.register(RemoveFirst(db, collectionManager))
        commandManager.register(Show(collectionManager))
        commandManager.register(SumOfPrice(collectionManager))
        commandManager.register(Update(io, db, collectionManager))

        return RequestHandler(commandManager, userManager)
    }
}
