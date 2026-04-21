package model

import java.io.Serializable

data class Coordinates(
    val x: Long, // Поле не может быть null
    val y: Float, // Максимальное значение поля: 519, Поле не может быть null
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }

    init {
        require(y <= 519) { "y должен быть меньше 519" }
    }
}
