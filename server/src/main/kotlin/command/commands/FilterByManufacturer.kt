package command.commands

import command.Command
import manager.CollectionManager
import model.CommandResult
import model.Product

class FilterByManufacturer(
    private val collectionManager: CollectionManager,
) : Command {
    override val name = "filter_by_manufacturer"
    override val description = "display the elements whose manufacturer field value matches value"

    override fun execute(
        args: String,
        product: Product?,
    ): CommandResult {
        val name = args.trim()
        if (name.isBlank()) {
            return CommandResult(false, "укажите имя производителя, например: filter_by_manufacturer NAME")
        }
        val result = collectionManager.filterByManufacturer(name).sorted()
        return if (result.isEmpty()) {
            CommandResult(true, "элементов с таким производителем не найдено")
        } else {
            CommandResult(true, "найдено: ${result.size}", result)
        }
    }
}
