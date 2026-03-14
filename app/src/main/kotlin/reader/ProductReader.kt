package reader

import io.IOHandler
import model.Product
import java.time.ZonedDateTime

class ProductReader(
    private val io: IOHandler,
) {
    fun read(): Product =
        Product(
            id = 1,
            name = io.readString("введите название продукта"),
            coordinates = CoordinatesReader(io).read(),
            creationDate = ZonedDateTime.now(),
            price = io.readPositiveLong("введите цену"),
            unitOfMeasure = UnitOfMeasureReader(io).read(),
            manufacturer = OrganizationReader(io).read(),
        )
}
