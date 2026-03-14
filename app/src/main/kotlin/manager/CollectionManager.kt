package manager

import io.IOHandler
import model.Product
import java.time.ZonedDateTime
import java.util.LinkedList

class CollectionManager(
    private val io: IOHandler,
) {
    private val list = LinkedList<Product>()
    private var currProductId = 1L
    private var currOrgId = 1L
    private val initDate: ZonedDateTime = ZonedDateTime.now()

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
        list.removeAt(list.indexOfFirst { it.id == id })
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
}
