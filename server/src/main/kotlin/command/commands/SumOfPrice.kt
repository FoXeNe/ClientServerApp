package command.commands

import command.Command
import manager.CollectionManager
import model.CommandResult
import model.Product

class SumOfPrice(
    private val collectionManager: CollectionManager,
) : Command {
    override val name = "sum_of_price"
    override val description = "get the sum of the values in the price field for all items"

    override fun execute(
        args: String,
        product: Product?,
    ): CommandResult = CommandResult(true, "сумма цен: ${collectionManager.sumOfPrice()}")
}
