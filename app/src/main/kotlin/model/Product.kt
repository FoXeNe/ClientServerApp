package model

import java.time.ZonedDateTime

data class Product(
    val id: Long, // Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    val name: String, // Поле не может быть null, Строка не может быть пустой
    val coordinates: Coordinates, // Поле не может быть null
    val creationDate: java.time.ZonedDateTime, // Поле не может быть null, Значение этого поля должно генерироваться автоматически
    val price: Long, // Значение поля должно быть больше 0
    val unitOfMeasure: UnitOfMeasure?, // Поле может быть null
    val organization: Organization, // Поле не может быть null
) : Comparable<Product> {
    init {
        require(id > 0) { "id должен быть больше 0" }
        require(name.isNotBlank()) { "имя продукта не должно быть пустым" }
        require(price > 0) { "цена должна быть больше 0" }
    }
    override fun compareTo(other: Product): Int = this.price.compareTo(other.price)
}
