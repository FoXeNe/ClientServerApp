package app

import io.ConsoleHandler
import io.IOWrapper
import manager.CommandManager
import manager.ConnectionManager
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.logging.Logger

class AppExecutor {
    private val logger = Logger.getLogger(AppExecutor::class.java.name)
    var interactiveMode = true

    fun exec() {
        logger.info("запуск сервера")
        val io = IOWrapper(ConsoleHandler())
        val manager = CommandManager()

        val requestHandler = AppInitializer().setup(manager, io, this)
        val port = System.getenv("SERVER_PORT")?.toIntOrNull() ?: 9090
        val connManager = ConnectionManager("0.0.0.0", port, requestHandler)

        val consoleReader = BufferedReader(InputStreamReader(System.`in`))

        while (interactiveMode) {
            connManager.exec()
            if (consoleReader.ready()) {
                val input = consoleReader.readLine()
                if (!input.isNullOrBlank()) {
                    logger.fine("команда: $input")
                    val result = manager.initCommand(input)
                    io.println(result.message)
                    result.collection?.forEach { io.println(it.toString()) }
                }
            }
        }

        logger.info("сервер завершает работу")
    }

    fun stop() {
        interactiveMode = false
    }
}
