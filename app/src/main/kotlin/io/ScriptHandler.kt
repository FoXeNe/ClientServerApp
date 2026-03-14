package io

import java.io.BufferedReader
import java.io.FileReader

class ScriptHandler(
    filePath: String,
) : IOHandler {
    private val reader = BufferedReader(FileReader(filePath))

    override fun println(message: String) {
        kotlin.io.println(message)
    }

    override fun readLine(): String? = reader.readLine()

    fun close() {
        reader.close()
    }
}
