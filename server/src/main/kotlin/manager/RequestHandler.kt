package manager

import model.Request
import model.Response

class RequestHandler(
    private val commandManager: CommandManager,
) {
    fun handle(request: Request): Response {
        return try {
            val commandName = request.commandType.name.lowercase()
            val command =
                commandManager.getCommands()[commandName]
                    ?: return Response(false, "неизвестная команда: $commandName")
            val args = request.argument ?: ""
            val result = command.execute(args, request.product)
            Response(result.success, result.message, result.collection)
        } catch (e: Exception) {
            Response(false, "ошибка: ${e.message}")
        }
    }
}
