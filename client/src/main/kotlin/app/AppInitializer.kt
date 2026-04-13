package app

import command.commands.*
import io.IOWrapper
import manager.CommandManager
import java.io.File
import java.util.LinkedList

const val ENV_FILE = "COLLECTION_FILE"

class AppInitializer {
    fun setup(
        commandManager: CommandManager,
        io: IOWrapper,
        app: AppExecutor,
    ) {
        commandManager.register(Help(io, commandManager))
        commandManager.register(History(io, commandManager))
        commandManager.register(Exit(io, { app.stop() }))
        commandManager.register(ExecuteScript(io, commandManager))
    }

    private fun createWalPath(mainFilePath: String?): String =
        if (mainFilePath != null) {
            "$mainFilePath.wal"
        } else {
            val tmpDir = System.getProperty("java.io.tmpdir")
            File(tmpDir, "collection_session.wal").absolutePath
        }
}
