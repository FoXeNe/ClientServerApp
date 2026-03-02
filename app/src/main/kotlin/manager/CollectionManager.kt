package manager

import io.IOHandler
import model.Product
import java.util.LinkedList

class CollectionManager(
    private val io: IOHandler,
) {
    private val list = LinkedList<Product>()
    private var currProductId = 1L

    fun addProduct(product: Product) {
        list.add(generateProductId(product))
        io.println(list.toString())
    }

    fun generateProductId(product: Product): Product {
        val productId = product.copy(id = currProductId)
        currProductId++
        return productId
    }
}
