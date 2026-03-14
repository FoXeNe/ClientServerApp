package io

interface IOHandler {
    fun println(message: String)

    fun readLine(): String?

    fun readLong(string: String): Long {
        while (true) {
            println(string)
            val input = readLine()?.trim()?.toLongOrNull()
            if (input != null) return input
            println("ожидался long")
        }
    }

    fun readFloat(string: String): Float {
        while (true) {
            println(string)
            val input = readLine()?.trim()?.toFloatOrNull()
            if (input != null) return input
            println("ожидался float")
        }
    }

    fun readBoundFloat(
        string: String,
        max: Float,
    ): Float {
        while (true) {
            val value = readFloat(string)
            if (value <= max) return value
            println("значение не должно превышать $max")
        }
    }

    fun readString(string: String): String {
        while (true) {
            println(string)
            val input = readLine()
            if (!input.isNullOrBlank()) return input
            println("ожидался string")
        }
    }

    fun readBoundString(
        string: String,
        maxLength: Int,
    ): String {
        while (true) {
            val value = readString(string)
            if (value.length <= maxLength) return value
            println("длина строки не должна превышать $maxLength")
        }
    }
}
