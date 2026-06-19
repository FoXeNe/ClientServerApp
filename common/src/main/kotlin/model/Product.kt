package model

import java.io.Serializable
import java.time.ZonedDateTime

data class Product(
    val id: Long,
    val name: String,
    val coordinates: Coordinates,
    val creationDate: java.time.ZonedDateTime,
    val price: Long,
    val unitOfMeasure: UnitOfMeasure?,
    val manufacturer: Organization,
    val owner: String? = null,
) : Comparable<Product>,
    Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }

    init {
        require(id > 0) { "id должен быть больше 0" }
        require(name.isNotBlank()) { "имя продукта не должно быть пустым" }
        require(price > 0) { "цена должна быть больше 0" }
    }

    override fun compareTo(other: Product): Int = this.price.compareTo(other.price)
}
