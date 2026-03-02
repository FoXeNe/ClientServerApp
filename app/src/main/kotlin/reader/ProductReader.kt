package reader

import io.IOHandler
import manager.CollectionManager
import model.Product
import reader.CoordinatesReader
import reader.OrganizationReader
import java.time.ZonedDateTime
import java.util.LinkedList
import kotlin.text.toLong
import kotlin.text.toLongOrNull

class ProductReader(
    private val io: IOHandler,
) {
    fun read(): Product {
        val newProduct =
            Product(
                id = 1,
                name = nameInput(),
                coordinates = CoordinatesReader(io).read(),
                creationDate = ZonedDateTime.now(),
                price = priceInput(),
                unitOfMeasure = null,
                organization = OrganizationReader(io).read(),
            )
        return newProduct
    }

    fun nameInput(): String {
        var name: String? = null
        while (name == null) {
            io.println("введите имя")
            name = io.readLine()
            if (name !is String || name == "") {
                io.println("имя должно быть string и иметь хотя бы один символ")
                name = null
            }
        }
        return name
    }

    fun priceInput(): Long {
        var price: Long? = null
        while (price == null) {
            io.println("введите цену")
            price = io.readLine()?.toLongOrNull()
            if (price !is Long) {
                io.println("цена должна быть long")
                price = null
            }
        }
        return price
    }
}
