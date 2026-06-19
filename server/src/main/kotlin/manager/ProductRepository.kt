package manager

import model.Coordinates
import model.Organization
import model.Product
import model.UnitOfMeasure
import java.sql.Connection
import java.time.OffsetDateTime

class ProductRepository(
    private val connection: Connection,
) {
    @Synchronized
    fun loadAll(): List<Pair<Product, String>> {
        val result = mutableListOf<Pair<Product, String>>()
        connection.createStatement().use { stmt ->
            val rs = stmt.executeQuery("SELECT * FROM products ORDER BY id")
            while (rs.next()) {
                val product =
                    Product(
                        id = rs.getLong("id"),
                        name = rs.getString("name"),
                        coordinates =
                            Coordinates(
                                x = rs.getLong("coord_x"),
                                y = rs.getFloat("coord_y"),
                            ),
                        creationDate = rs.getObject("creation_date", OffsetDateTime::class.java).toZonedDateTime(),
                        price = rs.getLong("price"),
                        unitOfMeasure = rs.getString("unit_of_measure")?.let { UnitOfMeasure.valueOf(it) },
                        manufacturer =
                            Organization(
                                id = rs.getLong("org_id"),
                                name = rs.getString("org_name"),
                                fullName = rs.getString("org_full_name"),
                                employeesCount = rs.getLong("org_employees_count"),
                            ),
                    )
                result.add(product to rs.getString("owner_login"))
            }
        }
        return result
    }

    @Synchronized
    fun insert(
        product: Product,
        ownerLogin: String,
    ): Product {
        val sql = """INSERT INTO products (name, coord_x, coord_y, creation_date, price, unit_of_measure,
                org_name, org_full_name, org_employees_count, owner_login)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id, org_id"""
        connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, product.name)
            stmt.setLong(2, product.coordinates.x)
            stmt.setFloat(3, product.coordinates.y)
            stmt.setObject(4, product.creationDate.toOffsetDateTime())
            stmt.setLong(5, product.price)
            stmt.setString(6, product.unitOfMeasure?.name)
            stmt.setString(7, product.manufacturer.name)
            stmt.setString(8, product.manufacturer.fullName)
            stmt.setLong(9, product.manufacturer.employeesCount)
            stmt.setString(10, ownerLogin)
            val rs = stmt.executeQuery()
            rs.next()
            return product.copy(
                id = rs.getLong("id"),
                manufacturer = product.manufacturer.copy(id = rs.getLong("org_id")),
            )
        }
    }

    @Synchronized
    fun update(
        product: Product,
        ownerLogin: String,
    ): Boolean {
        val sql = """UPDATE products SET name=?, coord_x=?, coord_y=?, price=?, unit_of_measure=?,
                org_name=?, org_full_name=?, org_employees_count=?
            WHERE id=? AND owner_login=?"""
        connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, product.name)
            stmt.setLong(2, product.coordinates.x)
            stmt.setFloat(3, product.coordinates.y)
            stmt.setLong(4, product.price)
            stmt.setString(5, product.unitOfMeasure?.name)
            stmt.setString(6, product.manufacturer.name)
            stmt.setString(7, product.manufacturer.fullName)
            stmt.setLong(8, product.manufacturer.employeesCount)
            stmt.setLong(9, product.id)
            stmt.setString(10, ownerLogin)
            return stmt.executeUpdate() > 0
        }
    }

    @Synchronized
    fun delete(
        id: Long,
        ownerLogin: String,
    ): Boolean =
        connection.prepareStatement("DELETE FROM products WHERE id=? AND owner_login=?").use { stmt ->
            stmt.setLong(1, id)
            stmt.setString(2, ownerLogin)
            stmt.executeUpdate() > 0
        }

    @Synchronized
    fun clearByOwner(ownerLogin: String): Int {
        val count =
            connection.prepareStatement("DELETE FROM products WHERE owner_login=?").use { stmt ->
                stmt.setString(1, ownerLogin)
                stmt.executeUpdate()
            }
        val isEmpty =
            connection.createStatement().use { stmt ->
                stmt.executeQuery("SELECT COUNT(*) FROM products").use { rs ->
                    rs.next()
                    rs.getLong(1) == 0L
                }
            }
        if (isEmpty) {
            connection.createStatement().use { stmt ->
                stmt.execute("ALTER SEQUENCE product_id_seq RESTART WITH 1")
                stmt.execute("ALTER SEQUENCE org_id_seq RESTART WITH 1")
            }
        }
        return count
    }
}
