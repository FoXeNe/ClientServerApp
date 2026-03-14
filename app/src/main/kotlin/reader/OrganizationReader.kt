package reader

import io.IOHandler
import model.Organization

class OrganizationReader(
    private val io: IOHandler,
) {
    fun read(): Organization {
        val name = io.readString("введите название организации")
        val fullName = io.readBoundString("введите полное название", 532)
        val employees = io.readPositiveLong("введите количество сотрудников")
        return Organization(1L, name, fullName, employees)
    }
}
