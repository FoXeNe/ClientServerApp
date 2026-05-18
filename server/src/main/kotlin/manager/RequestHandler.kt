package manager

import model.CommandType
import model.Request
import model.Response

class RequestHandler(
    private val commandManager: CommandManager,
    private val userManager: UserManager,
) {
    fun handle(request: Request): Response {
        return try {
            when (request.commandType) {
                CommandType.REGISTER -> {
                    val login = request.login ?: return Response(false, "укажите логин")
                    val password = request.password ?: return Response(false, "укажите пароль")
                    if (userManager.register(login, password)) {
                        Response(true, "пользователь зарегистрирован")
                    } else {
                        Response(false, "пользователь уже существует")
                    }
                }
                CommandType.LOGIN -> {
                    val login = request.login ?: return Response(false, "укажите логин")
                    val password = request.password ?: return Response(false, "укажите пароль")
                    if (userManager.authenticate(login, password)) {
                        Response(true, "авторизация успешна")
                    } else {
                        Response(false, "неверный логин или пароль")
                    }
                }
                else -> {
                    val login = request.login ?: return Response(false, "требуется авторизация")
                    val password = request.password ?: return Response(false, "требуется авторизация")
                    if (!userManager.authenticate(login, password)) {
                        return Response(false, "неверный логин или пароль")
                    }
                    val commandName = request.commandType.name.lowercase()
                    val command =
                        commandManager.getCommands()[commandName]
                            ?: return Response(false, "неизвестная команда: $commandName")
                    val result = command.execute(request.argument ?: "", request.product, login)
                    Response(result.success, result.message, result.collection)
                }
            }
        } catch (e: Exception) {
            Response(false, "ошибка: ${e.message}")
        }
    }
}
