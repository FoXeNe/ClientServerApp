package manager

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import model.Product
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.FileReader
import java.lang.reflect.Type
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.LinkedList

class JsonManager(
    private val filePath: String,
) {
    private class ZonedDateTimeAdapter :
        JsonSerializer<ZonedDateTime>,
        JsonDeserializer<ZonedDateTime> {
        override fun serialize(
            src: ZonedDateTime?,
            typeOfSrc: Type?,
            context: JsonSerializationContext?,
        ): JsonElement = JsonPrimitive(src?.format(DateTimeFormatter.ISO_ZONED_DATE_TIME))

        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?,
        ): ZonedDateTime? = json?.asString?.let { ZonedDateTime.parse(it, DateTimeFormatter.ISO_ZONED_DATE_TIME) }
    }

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
