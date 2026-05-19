package manager

import java.sql.Connection

class UserRepository(private val connection: Connection) {
    @Synchronized
    fun register(login: String, passwordHash: String): Boolean =
        try {
            connection.prepareStatement("INSERT INTO users (login, password_hash) VALUES (?, ?)").use { stmt ->
                stmt.setString(1, login)
                stmt.setString(2, passwordHash)
                stmt.executeUpdate()
            }
            true
        } catch (e: Exception) {
            false
        }

    @Synchronized
    fun getPasswordHash(login: String): String? =
        connection.prepareStatement("SELECT password_hash FROM users WHERE login=?").use { stmt ->
            stmt.setString(1, login)
            val rs = stmt.executeQuery()
            if (rs.next()) rs.getString("password_hash") else null
        }
}
