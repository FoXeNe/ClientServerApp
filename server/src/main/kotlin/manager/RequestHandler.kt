package manager

import model.CommandType
import model.Request
import model.Response
import java.util.stream.Collectors

class RequestHandler(
    private val collectionManager: CollectionManager,
) {
    fun handle(request: Request): Response {
        return try {
            when (request.commandType) {
                CommandType.ADD -> {
                    val product =
                        request.product
                            ?: return Response(false, "продукт не указан")
                    collectionManager.addProduct(product)
                    Response(true, "продукт добавлен")
                }

                CommandType.ADD_IF_MIN -> {
                    val product =
                        request.product
                            ?: return Response(false, "продукт не указан")
                    val min = collectionManager.getMinProduct()
                    if (min == null || product < min) {
                        collectionManager.addProduct(product)
                        Response(true, "продукт добавлен")
                    } else {
                        Response(true, "цена не меньше минимальной")
                    }
                }

                CommandType.CLEAR -> {
                    collectionManager.clear()
                    Response(true, "коллекция очищена")
                }

                CommandType.FILTER_BY_MANUFACTURER -> {
                    val name =
                        request.argument
                            ?: return Response(false, "имя производителя не указано")
                    val sorted =
                        collectionManager
                            .filterByManufacturer(name)
                            .stream()
                            .sorted()
                            .collect(Collectors.toList())
                    Response(true, "найдено: ${sorted.size}", sorted)
                }

                CommandType.FILTER_GREATER_THAN_MANUFACTURER -> {
                    val name =
                        request.argument
                            ?: return Response(false, "имя производителя не указано")
                    val sorted =
                        collectionManager
                            .filterGreaterThanManufacturer(name)
                            .stream()
                            .sorted()
                            .collect(Collectors.toList())
                    Response(true, "найдено: ${sorted.size}", sorted)
                }

                CommandType.INFO -> {
                    Response(true, collectionManager.getInfoString())
                }

                CommandType.REMOVE_BY_ID -> {
                    val id =
                        request.argument?.trim()?.toLongOrNull()
                            ?: return Response(false, "id не указан или некорректен")
                    if (collectionManager.getCollection().stream().noneMatch { it.id == id }) {
                        return Response(false, "элемент с id=$id не найден")
                    }
                    collectionManager.removeById(id)
                    Response(true, "элемент удалён")
                }

                CommandType.REMOVE_FIRST -> {
                    if (collectionManager.getCollection().isEmpty()) {
                        return Response(false, "коллекция пуста")
                    }
                    collectionManager.removeFirst()
                    Response(true, "первый элемент удалён")
                }

                CommandType.SHOW -> {
                    val sorted =
                        collectionManager
                            .getCollection()
                            .stream()
                            .sorted()
                            .collect(Collectors.toList())
                    val msg = if (sorted.isEmpty()) "коллекция пустая" else "элементов: ${sorted.size}"
                    Response(true, msg, sorted)
                }

                CommandType.SUM_OF_PRICE -> {
                    val sum = collectionManager.sumOfPrice()
                    Response(true, "сумма цен: $sum")
                }

                CommandType.UPDATE -> {
                    val id =
                        request.argument?.trim()?.toLongOrNull()
                            ?: return Response(false, "id не указан или некорректен")
                    val product =
                        request.product
                            ?: return Response(false, "продукт не указан")
                    if (collectionManager.getCollection().stream().noneMatch { it.id == id }) {
                        return Response(false, "элемент с id=$id не найден")
                    }
                    collectionManager.updateById(id, product)
                    Response(true, "элемент обновлён")
                }
            }
        } catch (e: Exception) {
            Response(false, "ошибка: ${e.message}")
        }
    }
}
