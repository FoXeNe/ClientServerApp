package app

import command.CommandManager
import command.commands.Add
import command.commands.Clear
import command.commands.Exit
import io.IOHandler
import manager.CollectionManager

class AppInitializer {
    fun setup(
        manager: CommandManager,
        io: IOHandler,
        app: AppExecutor,
    ) {
        val collectionManager = CollectionManager(io)

        manager.register(Add(io, collectionManager))
        manager.register(Exit(io) { app.stop() })
        manager.register(Clear(io, collectionManager))
    }
}
