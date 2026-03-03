package reader

import io.IOHandler
import manager.CollectionManager
import model.Product
import reader.CoordinatesReader
import reader.OrganizationReader
import reader.UnitOfMeasureReader
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
                name = io.readString("введите название"),
                coordinates = CoordinatesReader(io).read(),
                creationDate = ZonedDateTime.now(),
                price = io.readLong("введите цену"),
                unitOfMeasure = UnitOfMeasureReader(io).read(),
                manufacturer = OrganizationReader(io).read(),
            )
        return newProduct
    }
}
