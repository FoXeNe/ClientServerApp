package manager

import io.IOHandler
import model.Product
import java.time.ZonedDateTime
import java.util.LinkedList

class CollectionManager(
    private val io: IOHandler,
    private val filePath: String?,
) {
    private val list = LinkedList<Product>()
    private var currProductId = 1L
    private var currOrgId = 1L
    private val initDate: ZonedDateTime = ZonedDateTime.now()

    init {
        if (filePath != null) loadFromFile(filePath)
    }

    fun loadFromFile(path: String) {
        val loaded = JsonManager(path).readCollection()
        list.clear()
        list.addAll(loaded)
        if (list.isNotEmpty()) {
            currProductId = list.maxOf { it.id } + 1
            currOrgId = list.maxOf { it.manufacturer.id } + 1
        }
        io.println("коллекция загружена из файла: $path")
    }

    fun saveToFile(path: String? = filePath) {
        if (path == null) {
            io.println("путь к файлу не задан")
            return
        }
        JsonManager(path).writeCollection(list)
        io.println("коллекция сохранена в файл: $path")
    }

    fun addProduct(product: Product) {
        list.add(generateId(product))
        io.println("продукт добавлен")
    }

    fun generateId(product: Product): Product =
        product.copy(
            id = currProductId++,
            manufacturer = product.manufacturer.copy(id = currOrgId++),
        )

    fun getInfoString(): String =
        """
        тип: ${list.javaClass.name}
        дата инициализации: $initDate
        количество элементов: ${list.size}
        """.trimIndent()

    fun updateById(
        id: Long,
        newProduct: Product,
    ) {
        val index = list.indexOfFirst { it.id == id }
        val old = list[index]
        list[index] =
            newProduct.copy(
                id = old.id,
                creationDate = old.creationDate,
                manufacturer = newProduct.manufacturer.copy(id = old.manufacturer.id),
            )
        io.println("элемент обновлён")
    }

    fun removeById(id: Long) {
        list.removeAll { it.id == id }
        io.println("элемент удалён")
    }

    fun removeFirst() {
        list.removeFirst()
        io.println("первый элемент удалён")
    }

    fun clear() {
        list.clear()
        io.println("коллекция очищена")
    }

    fun getCollection(): LinkedList<Product> = list

    fun getMinProduct(): Product? = list.minOrNull()

    fun sumOfPrice(): Long {
        var res = 0L
        for (i in list) {
            res += i.price
        }
        return res
    }

    fun filterByManufacturer(manufacturerName: String): List<Product> {
        val result = mutableListOf<Product>()
        for (product in list) {
            if (product.manufacturer.name == manufacturerName) {
                result.add(product)
            }
        }
        return result
    }

    fun filterGreaterThanManufacturer(manufacturerName: String): List<Product> {
        val result = mutableListOf<Product>()
        for (product in list) {
            if (product.manufacturer.name > manufacturerName) {
                result.add(product)
            }
        }
        return result
    }
}
