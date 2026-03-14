package reader

import io.IOHandler
import model.Coordinates

class CoordinatesReader(
    private val io: IOHandler,
) {
    fun read(): Coordinates {
        val x = io.readLong("введите x")
        val y = io.readBoundFloat("введите y", 519f)
        return Coordinates(x, y)
    }
}
