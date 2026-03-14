package command.commands

import command.Command
import io.IOWrapper
import io.ScriptHandler
import manager.CommandManager
import java.io.File
import java.io.FileNotFoundException

class ExecuteScript(
    private val io: IOWrapper,
    private val commandManager: CommandManager,
) : Command {
    override val name = "execute_script"
    override val description = "read and execute script"

    override fun execute(args: String) {
        val fileName = args.trim()
        if (fileName.isBlank()) {
            io.println("укажите имя файла, к примеру: execute_script script.txt")
            return
        }

        val file = File(fileName)
        if (!file.exists()) {
            io.println("файл не найден")
            return
        }

        val scriptHandler =
            try {
                ScriptHandler(fileName)
            } catch (e: FileNotFoundException) {
                io.println("не удалось открыть файл $fileName")
                return
            }

        val previousSwap = io.swapHandler(scriptHandler)
        try {
            var line = io.readLine()
            while (line != null) {
                if (line.isNotBlank()) {
                    io.println("> $line")
                    commandManager.initCommand(line, io)
                }
                line = io.readLine()
            }
        } finally {
            io.swapHandler(previousSwap)
            scriptHandler.close()
        }
    }
}
