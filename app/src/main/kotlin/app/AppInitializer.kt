package app

import command.CommandManager
import command.commands.Add
import command.commands.Clear
import command.commands.Exit
import command.commands.Help
import command.commands.Info
import command.commands.RemoveById
import command.commands.RemoveFirst
import command.commands.Show
import command.commands.Update
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
        commandManager.register(Show(io, collectionManager))
        commandManager.register(Info(io, collectionManager))
        commandManager.register(Update(io, collectionManager))
        commandManager.register(RemoveById(io, collectionManager))
        commandManager.register(RemoveFirst(io, collectionManager))
    }
}
