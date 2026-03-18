package manager

import model.Product

sealed class WalEntry {
    data class Add(
        val product: Product,
    ) : WalEntry()

    data class Update(
        val id: Long,
        val product: Product,
    ) : WalEntry()

    data class RemoveById(
        val id: Long,
    ) : WalEntry()

    data object RemoveFirst : WalEntry()

    data object Clear : WalEntry()
}
