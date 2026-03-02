package manager

import io.IOHandler
import model.Product
import java.util.LinkedList

class CollectionManager(
    private val io: IOHandler,
) {
    private val list = LinkedList<Product>()
    private var currProductId = 1L
    private var currOrgId = 1L

    fun addProduct(product: Product) {
        list.add(generateId(product))
        io.println(list.toString())
    }

    fun generateId(product: Product): Product {
        val res =
            product.copy(
                id = currProductId++,
                organization = product.organization.copy(id = currOrgId++),
            )
        return res
    }
}
