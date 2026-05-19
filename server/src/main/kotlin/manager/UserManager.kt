package manager

import java.security.MessageDigest

class UserManager(private val repo: UserRepository) {
    fun register(login: String, password: String): Boolean {
        if (login.isBlank() || password.isBlank()) return false
        return repo.register(login, hash(password))
    }

    fun authenticate(login: String, password: String): Boolean {
        val stored = repo.getPasswordHash(login) ?: return false
        return stored == hash(password)
    }

    private fun hash(password: String): String {
        val digest = MessageDigest.getInstance("SHA-224")
        val bytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
