package manager

import com.google.gson.GsonBuilder
import model.Product
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.time.ZonedDateTime

class WalManager(
    private val walPath: String,
) {
    private data class WalRecord(
        val type: String,
        val id: Long? = null,
        val product: Product? = null,
    )

    private val gson =
        GsonBuilder()
            .registerTypeAdapter(ZonedDateTime::class.java, ZonedDateTimeAdapter())
            .create()

    fun append(entry: WalEntry) {
        val record =
            when (entry) {
                is WalEntry.Add -> WalRecord("ADD", product = entry.product)
                is WalEntry.Update -> WalRecord("UPDATE", id = entry.id, product = entry.product)
                is WalEntry.RemoveById -> WalRecord("REMOVE_BY_ID", id = entry.id)
                is WalEntry.RemoveFirst -> WalRecord("REMOVE_FIRST")
                is WalEntry.Clear -> WalRecord("CLEAR")
            }
        BufferedWriter(FileWriter(walPath, true)).use { writer ->
            writer.write(gson.toJson(record))
            writer.newLine()
        }
    }

    fun readAll(): List<WalEntry> {
        val file = File(walPath)
        if (!file.exists()) return emptyList()
        return file
            .readLines()
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                try {
                    val record = gson.fromJson(line, WalRecord::class.java)
                    when (record.type) {
                        "ADD" -> {
                            record.product?.let { WalEntry.Add(it) }
                        }

                        "UPDATE" -> {
                            if (record.id != null && record.product != null) {
                                WalEntry.Update(record.id, record.product)
                            } else {
                                null
                            }
                        }

                        "REMOVE_BY_ID" -> {
                            record.id?.let { WalEntry.RemoveById(it) }
                        }

                        "REMOVE_FIRST" -> {
                            WalEntry.RemoveFirst
                        }

                        "CLEAR" -> {
                            WalEntry.Clear
                        }

                        else -> {
                            null
                        }
                    }
                } catch (e: Exception) {
                    null
                }
            }
    }

    fun clear() {
        File(walPath).delete()
    }

    fun hasEntries(): Boolean {
        val file = File(walPath)
        return file.exists() && file.length() > 0
    }
}
