package manager

import io.IOHandler
import model.Product
import java.time.ZonedDateTime
import java.util.LinkedList

class CollectionManager(
    private val io: IOHandler,
    collection: LinkedList<Product> = LinkedList(),
    private val walManager: WalManager? = null,
) {
    private val list = LinkedList<Product>(collection)
    private var currProductId: Long
    private var currOrgId: Long
    private val initDate: ZonedDateTime = ZonedDateTime.now()

    init {
        currProductId = if (list.isEmpty()) 1L else list.maxOf { it.id } + 1
        currOrgId = if (list.isEmpty()) 1L else list.maxOf { it.manufacturer.id } + 1
    }

    fun addProduct(product: Product) {
        val withId = generateId(product)
        walManager?.append(WalEntry.Add(withId))
        list.add(withId)
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
        val updated =
            newProduct.copy(
                id = old.id,
                creationDate = old.creationDate,
                manufacturer = newProduct.manufacturer.copy(id = old.manufacturer.id),
            )
        walManager?.append(WalEntry.Update(id, updated))
        list[index] = updated
        io.println("элемент обновлён")
    }

    fun removeById(id: Long) {
        walManager?.append(WalEntry.RemoveById(id))
        list.removeAll { it.id == id }
        io.println("элемент удалён")
    }

    fun removeFirst() {
        walManager?.append(WalEntry.RemoveFirst)
        list.removeFirst()
        io.println("первый элемент удалён")
    }

    fun clear() {
        walManager?.append(WalEntry.Clear)
        list.clear()
        io.println("коллекция очищена")
    }

    fun getCollection(): LinkedList<Product> = list

    fun getMinProduct(): Product? = list.minOrNull()

    fun sumOfPrice(): Long = list.sumOf { it.price }

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

    fun replayEntry(entry: WalEntry) {
        when (entry) {
            is WalEntry.Add -> {
                list.add(entry.product)
                if (entry.product.id >= currProductId) currProductId = entry.product.id + 1
                if (entry.product.manufacturer.id >= currOrgId) currOrgId = entry.product.manufacturer.id + 1
            }

            is WalEntry.Update -> {
                val index = list.indexOfFirst { it.id == entry.id }
                if (index >= 0) list[index] = entry.product
            }

            is WalEntry.RemoveById -> {
                list.removeAll { it.id == entry.id }
            }

            is WalEntry.RemoveFirst -> {
                if (list.isNotEmpty()) list.removeFirst()
            }

            is WalEntry.Clear -> {
                list.clear()
            }
        }
    }

    fun replaceCollection(newCollection: LinkedList<Product>) {
        list.clear()
        list.addAll(newCollection)
        currProductId = if (list.isEmpty()) 1L else list.maxOf { it.id } + 1
        currOrgId = if (list.isEmpty()) 1L else list.maxOf { it.manufacturer.id } + 1
    }
}
