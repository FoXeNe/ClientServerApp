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
    private var initDate = ZonedDateTime.now()

    fun addProduct(product: Product) {
        list.add(generateId(product))
        io.println("продукт добавлен")
    }

    fun generateId(product: Product): Product {
        val res =
            product.copy(
                id = currProductId++,
                manufacturer = product.manufacturer.copy(id = currOrgId++),
            )
        return res
    }

    fun getInfoString(): String =
        """
        тип: ${list.javaClass.name}
        дата инициализации: $initDate
        количество элементов: ${list.size}
      """

    fun updateById(
        id: Long,
        newProduct: Product,
    ) {
        val index = list.indexOfFirst { it.id == id }

        val old = list[index]

        val updated =
            newProduct.copy(
                id = old.id,
                creationDate = old.creationDate,
                manufacturer = newProduct.manufacturer.copy(id = old.manufacturer.id),
            )

        list[index] = updated
        io.println("элемент обновлен")
    }

    fun removeById(id: Long) {
        val index = list.indexOfFirst { it.id == id }
        list.removeAt(index)
        io.println("элемент удален")
    }

    fun removeFirst() {
        list.removeFirst()
        io.println("первый элемент удален")
    }

    fun getCollection(): LinkedList<Product> = list

    fun clear() {
        list.clear()
    }
}
