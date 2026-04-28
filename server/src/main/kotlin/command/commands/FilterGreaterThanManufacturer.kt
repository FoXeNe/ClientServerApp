package command.commands

import command.Command
import manager.CollectionManager
import model.CommandResult
import model.Product

class FilterGreaterThanManufacturer(
    private val collectionManager: CollectionManager,
) : Command {
    override val name = "filter_greater_than_manufacturer"
    override val description = "return the elements whose manufacturer field value is greater than value"

    override fun execute(
        args: String,
        product: Product?,
    ): CommandResult {
        val name = args.trim()
        if (name.isBlank()) {
            return CommandResult(false, "укажите имя производителя, например: filter_greater_than_manufacturer NAME")
        }
        val result = collectionManager.filterGreaterThanManufacturer(name).sorted()
        return if (result.isEmpty()) {
            CommandResult(true, "элементов с производителем больше не найдено")
        } else {
            CommandResult(true, "найдено: ${result.size}", result)
        }
    }
}
