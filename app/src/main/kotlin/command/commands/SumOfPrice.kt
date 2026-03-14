package command.commands

import command.Command
import io.IOHandler
import manager.CollectionManager

class SumOfPrice(
    private val io: IOHandler,
    private val collectionManager: CollectionManager,
) : Command {
    override val name = "sum_of_price"
    override val description = "get the sum of the values in the price field for all items"

    override fun execute(args: String) {
        io.println("сумма цен: ${collectionManager.sumOfPrice()}")
    }
}
