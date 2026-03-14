package app

import command.commands.*
import io.IOWrapper
import manager.CollectionManager
import manager.CommandManager

const val ENV_FILE = "COLLECTION_FILE"

class AppInitializer {
    fun setup(
        commandManager: CommandManager,
        io: IOWrapper,
        app: AppExecutor,
    ) {

        val filePath = System.getenv(ENV_FILE)
        if (filePath == null) {
            io.println("$ENV_FILE не задан")
        }
        val collectionManager = CollectionManager(io, filePath)

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
        commandManager.register(Update(io, collectionManager))
        commandManager.register(Save(io, collectionManager))
        commandManager.register(SumOfPrice(io, collectionManager))
    }
}
