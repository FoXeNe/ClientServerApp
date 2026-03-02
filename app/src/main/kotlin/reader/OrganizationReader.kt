package reader

import io.IOHandler
import model.Organization

class OrganizationReader(
    private val io: IOHandler,
) {
    fun read(): Organization {
        val id = 1L

        io.println("введите название")
        val name = io.readLine().toString()

        io.println("введите полное имя")
        val fullName = io.readLine().toString()

        io.println("введите количество участников")
        val employees = io.readLine()!!.toLong()

        return Organization(id, name, fullName, employees)
    }
}
