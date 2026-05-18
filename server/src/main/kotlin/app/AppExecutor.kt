package app

import io.ConsoleHandler
import io.IOWrapper
import manager.CommandManager
import manager.ConnectionManager
import java.util.logging.Logger

class AppExecutor {
    private val logger = Logger.getLogger(AppExecutor::class.java.name)

    fun exec() {
        logger.info("запуск сервера")
        val io = IOWrapper(ConsoleHandler())
        val manager = CommandManager()
        val requestHandler = AppInitializer().setup(manager, io)
        val port = System.getenv("SERVER_PORT")?.toIntOrNull() ?: 45205
        val connManager = ConnectionManager("0.0.0.0", port, requestHandler)
        Runtime.getRuntime().addShutdownHook(Thread { connManager.close() })
        connManager.exec()
    }
}
