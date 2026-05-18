package manager

import model.Product
import java.time.ZonedDateTime
import java.util.LinkedList
import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.stream.Collectors
import kotlin.concurrent.withLock

class CollectionManager(
    initialData: List<Pair<Product, String>> = emptyList(),
) {
    private val list = LinkedList<Product>()
    private val ownerMap = mutableMapOf<Long, String>()
    private val lock = ReentrantReadWriteLock()
    private val initDate: ZonedDateTime = ZonedDateTime.now()

    init {
        for ((product, owner) in initialData) {
            list.add(product)
            ownerMap[product.id] = owner
        }
    }

    fun addProduct(product: Product, ownerLogin: String) {
        lock.writeLock().withLock {
            list.add(product)
            ownerMap[product.id] = ownerLogin
        }
    }

    fun getInfoString(): String =
        lock.readLock().withLock {
            """
            тип: ${list.javaClass.name}
            дата инициализации: $initDate
            количество элементов: ${list.size}
            """.trimIndent()
        }

    fun updateById(
        id: Long,
        updated: Product,
        ownerLogin: String,
    ): Boolean =
        lock.writeLock().withLock {
            val index = list.indexOfFirst { it.id == id }
            if (index < 0) return@withLock false
            if (ownerMap[id] != ownerLogin) return@withLock false
            list[index] = updated
            true
        }

    fun removeById(
        id: Long,
        ownerLogin: String,
    ): Boolean =
        lock.writeLock().withLock {
            if (ownerMap[id] != ownerLogin) return@withLock false
            list.removeAll { it.id == id }
            ownerMap.remove(id)
            true
        }

    fun clear(ownerLogin: String) {
        lock.writeLock().withLock {
            val toRemove = ownerMap.entries.filter { it.value == ownerLogin }.map { it.key }.toSet()
            list.removeAll { it.id in toRemove }
            toRemove.forEach { ownerMap.remove(it) }
        }
    }

    fun getCollection(): LinkedList<Product> =
        lock.readLock().withLock { LinkedList(list) }

    fun getById(id: Long): Product? =
        lock.readLock().withLock { list.find { it.id == id } }

    fun getFirst(): Product? =
        lock.readLock().withLock { list.firstOrNull() }

    fun getMinProduct(): Product? =
        lock.readLock().withLock {
            list
                .stream()
                .min(Comparator.naturalOrder())
                .orElse(null)
        }

    fun sumOfPrice(): Long =
        lock.readLock().withLock {
            list
                .stream()
                .mapToLong { it.price }
                .sum()
        }

    fun filterByManufacturer(manufacturerName: String): List<Product> =
        lock.readLock().withLock {
            list
                .stream()
                .filter { it.manufacturer.name == manufacturerName }
                .collect(Collectors.toList())
        }

    fun filterGreaterThanManufacturer(manufacturerName: String): List<Product> =
        lock.readLock().withLock {
            list
                .stream()
                .filter { it.manufacturer.name > manufacturerName }
                .collect(Collectors.toList())
        }

    fun hasId(id: Long): Boolean =
        lock.readLock().withLock { list.any { it.id == id } }

    fun getOwner(id: Long): String? =
        lock.readLock().withLock { ownerMap[id] }
}
