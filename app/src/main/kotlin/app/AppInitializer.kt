package app

import command.commands.*
import io.IOWrapper
import manager.CollectionManager
import manager.CommandManager

class AppInitializer {
    fun setup(
        commandManager: CommandManager,
        io: IOWrapper,
        app: AppExecutor,
    ) {
        val collectionManager = CollectionManager(io)

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
    }
}
