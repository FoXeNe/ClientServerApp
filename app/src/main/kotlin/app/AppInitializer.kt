package app

import command.CommandManager
import command.commands.Add
import command.commands.Clear
import command.commands.Exit
import command.commands.Help
import io.IOHandler
import manager.CollectionManager

class AppInitializer {
    fun setup(
        commandManager: CommandManager,
        io: IOHandler,
        app: AppExecutor,
    ) {
        val collectionManager = CollectionManager(io)

        commandManager.register(Add(io, collectionManager))
        commandManager.register(Exit(io) { app.stop() })
        commandManager.register(Clear(io, collectionManager))
        commandManager.register(Help(io, commandManager))
    }
}
