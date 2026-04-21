package model

import java.io.Serializable

data class Organization(
    val id: Long, // Поле не может быть null, Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    val name: String, // Поле не может быть null, Строка не может быть пустой
    val fullName: String, // Длина строки не должна быть больше 532, Строка не может быть пустой, Поле не может быть null
    val employeesCount: Long, // Значение поля должно быть больше 0
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }

    init {
        require(id > 0) { "id должен быть больше 0" }
        require(name.isNotBlank()) { "name не может быть пустым" }
        require(fullName.length <= 532 && fullName.isNotBlank()) { "fullName не должна быть больше 532 и строка не может быть пустой" }
        require(employeesCount > 0) { "employeesCount должно быть больше 0" }
    }
}
