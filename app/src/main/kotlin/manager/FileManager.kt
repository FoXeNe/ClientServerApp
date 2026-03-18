package manager

import io.IOHandler
import model.Product
import java.io.File
import java.nio.file.Files
import java.util.LinkedList

class FileManager(
    val mainFilePath: String?,
    private val io: IOHandler,
) {
    private var currentFilePath: String? = mainFilePath
    private var usingTempFile: Boolean = false

    fun isMainValid(): Boolean {
        val path = mainFilePath ?: return false
        val file = File(path)
        return try {
            if (file.exists()) {
                file.canRead() && file.canWrite()
            } else {
                file.parentFile?.canWrite() != false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun getCurrentFilePath(): String? = currentFilePath

    fun isUsingTempFile(): Boolean = usingTempFile

    fun tryWrite(collection: LinkedList<Product>): Boolean {
        val path = currentFilePath ?: return false
        return try {
            JsonManager(path).writeCollection(collection)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun offerTempFile(collection: LinkedList<Product>): Boolean {
        io.println("основной файл недоступен. Создать временный файл? (Y/n)")
        val answer = io.readLine()?.trim()?.lowercase()
        if (answer.isNullOrEmpty() || answer == "y" || answer == "yes") {
            return try {
                val tempPath = Files.createTempFile("collection_", ".json").toString()
                JsonManager(tempPath).writeCollection(collection)
                currentFilePath = tempPath
                usingTempFile = true
                io.println("создан временный файл: $tempPath")
                true
            } catch (e: Exception) {
                io.println("не удалось создать временный файл: ${e.message}")
                false
            }
        }
        return false
    }

    fun restoreMainFile(currentCollection: LinkedList<Product>): LinkedList<Product>? {
        val main = mainFilePath ?: return null
        if (!usingTempFile) return null
        if (!isMainValid()) return null
        return try {
            val mainCollection: LinkedList<Product> =
                try {
                    val file = File(main)
                    if (file.exists() && file.length() > 0) {
                        JsonManager(main).readCollection()
                    } else {
                        LinkedList()
                    }
                } catch (e: Exception) {
                    LinkedList()
                }

            val merged = LinkedList<Product>()
            val seenIds = mutableSetOf<Long>()
            for (i in mainCollection) {
                if (seenIds.add(i.id)) merged.add(i)
            }
            for (i in currentCollection) {
                if (seenIds.add(i.id)) merged.add(i)
            }

            JsonManager(main).writeCollection(merged)

            val tempPath = currentFilePath
            currentFilePath = main
            usingTempFile = false
            if (tempPath != null && tempPath != main) {
                File(tempPath).delete()
            }

            io.println("данные сохранены в $main")
            merged
        } catch (e: Exception) {
            io.println("ошибка : ${e.message}")
            null
        }
    }
}
