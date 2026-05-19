package manager

import java.sql.DriverManager
import java.util.logging.Logger

class DatabaseManager(url: String, user: String, password: String) {
    private val logger = Logger.getLogger(DatabaseManager::class.java.name)
    private val connection = DriverManager.getConnection(url, user, password)

    val users = UserRepository(connection)
    val products = ProductRepository(connection)

    init {
        connection.autoCommit = true
        createSchema()
    }

    private fun createSchema() {
        connection.createStatement().use { stmt ->
            stmt.execute(
                """CREATE TABLE IF NOT EXISTS users (
                    login VARCHAR(255) PRIMARY KEY,
                    password_hash VARCHAR(64) NOT NULL
                )""",
            )
            stmt.execute("CREATE SEQUENCE IF NOT EXISTS product_id_seq START 1")
            stmt.execute("CREATE SEQUENCE IF NOT EXISTS org_id_seq START 1")
            stmt.execute(
                """CREATE TABLE IF NOT EXISTS products (
                    id BIGINT PRIMARY KEY DEFAULT nextval('product_id_seq'),
                    name VARCHAR(255) NOT NULL,
                    coord_x BIGINT NOT NULL,
                    coord_y REAL NOT NULL,
                    creation_date TIMESTAMPTZ NOT NULL,
                    price BIGINT NOT NULL,
                    unit_of_measure VARCHAR(50),
                    org_id BIGINT NOT NULL DEFAULT nextval('org_id_seq'),
                    org_name VARCHAR(255) NOT NULL,
                    org_full_name VARCHAR(532) NOT NULL,
                    org_employees_count BIGINT NOT NULL,
                    owner_login VARCHAR(255) NOT NULL REFERENCES users(login)
                )""",
            )
        }
        logger.info("схема БД создана")
    }
}
