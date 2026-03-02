package command

interface Command {
    val name: String
    val description: String

    fun execute()
}
