package manager

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import model.Product
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.FileReader
import java.time.ZonedDateTime
import java.util.LinkedList

class JsonManager(
    private val filePath: String,
) {
    private val gson =
        GsonBuilder()
            .registerTypeAdapter(ZonedDateTime::class.java, ZonedDateTimeAdapter())
            .setPrettyPrinting()
            .create()

    fun readCollection(): LinkedList<Product> =
        BufferedReader(FileReader(filePath)).use { reader ->
            val json = reader.readText()
            val type = object : TypeToken<LinkedList<Product>>() {}.type
            gson.fromJson<LinkedList<Product>>(json, type) ?: LinkedList()
        }

    fun writeCollection(collection: LinkedList<Product>) {
        val json = gson.toJson(collection)
        BufferedOutputStream(FileOutputStream(filePath)).use { out ->
            out.write(json.toByteArray(Charsets.UTF_8))
        }
    }
}
