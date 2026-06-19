package gui.view

import gui.LocaleManager
import javafx.event.ActionEvent
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import model.Coordinates
import model.Organization
import model.Product
import model.UnitOfMeasure
import java.time.ZonedDateTime

class ProductDialog(existing: Product? = null) : Dialog<Product>() {

    private val nameField = TextField(existing?.name ?: "")
    private val xField = TextField(existing?.coordinates?.x?.toString() ?: "")
    private val yField = TextField(existing?.coordinates?.y?.toString() ?: "")
    private val priceField = TextField(existing?.price?.toString() ?: "")
    private val unitCombo = ComboBox<String>()
    private val orgNameField = TextField(existing?.manufacturer?.name ?: "")
    private val orgFullNameField = TextField(existing?.manufacturer?.fullName ?: "")
    private val orgEmployeesField = TextField(existing?.manufacturer?.employeesCount?.toString() ?: "")

    init {
        title = if (existing == null) LocaleManager.getString("form.add.title")
                else LocaleManager.getString("form.edit.title")

        val units = listOf("-") + UnitOfMeasure.values().map { it.name }
        unitCombo.items.addAll(units)
        unitCombo.value = existing?.unitOfMeasure?.name ?: "-"

        val grid = GridPane().apply {
            hgap = 10.0; vgap = 8.0; padding = Insets(16.0)
        }

        fun row(label: String, node: Node, row: Int) {
            grid.add(Label(label), 0, row)
            grid.add(node, 1, row)
        }

        row(LocaleManager.getString("form.name"), nameField, 0)
        row(LocaleManager.getString("form.x"), xField, 1)
        row(LocaleManager.getString("form.y"), yField, 2)
        row(LocaleManager.getString("form.price"), priceField, 3)
        row(LocaleManager.getString("form.unit"), unitCombo, 4)
        row(LocaleManager.getString("form.org_name"), orgNameField, 5)
        row(LocaleManager.getString("form.org_fullname"), orgFullNameField, 6)
        row(LocaleManager.getString("form.org_employees"), orgEmployeesField, 7)

        listOf(nameField, xField, yField, priceField, orgNameField, orgFullNameField, orgEmployeesField)
            .forEach { it.prefWidth = 260.0 }

        dialogPane.content = grid
        dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)

        val okButton = dialogPane.lookupButton(ButtonType.OK) as Button
        okButton.text = LocaleManager.getString("btn.ok")
        (dialogPane.lookupButton(ButtonType.CANCEL) as Button).text = LocaleManager.getString("btn.cancel")

        val errorLabel = Label().apply { style = "-fx-text-fill: red;" }
        grid.add(errorLabel, 0, 8, 2, 1)

        okButton.addEventFilter(ActionEvent.ACTION) { e ->
            val err = validate()
            if (err != null) { errorLabel.text = err; e.consume() }
        }

        setResultConverter { btn ->
            if (btn == ButtonType.OK) buildProduct(existing) else null
        }
    }

    private fun validate(): String? {
        if (nameField.text.isBlank()) return LocaleManager.getString("validation.name_empty")
        if (xField.text.trim().toLongOrNull() == null) return LocaleManager.getString("validation.x_invalid")
        val y = yField.text.trim().toFloatOrNull() ?: return LocaleManager.getString("validation.y_invalid")
        if (y > 519) return LocaleManager.getString("validation.y_max")
        val price = priceField.text.trim().toLongOrNull() ?: return LocaleManager.getString("validation.price_invalid")
        if (price <= 0) return LocaleManager.getString("validation.price_min")
        if (orgNameField.text.isBlank()) return LocaleManager.getString("validation.org_name_empty")
        if (orgFullNameField.text.isBlank() || orgFullNameField.text.length > 532) return LocaleManager.getString("validation.org_fullname_invalid")
        val emp = orgEmployeesField.text.trim().toLongOrNull() ?: return LocaleManager.getString("validation.employees_invalid")
        if (emp <= 0) return LocaleManager.getString("validation.employees_min")
        return null
    }

    private fun buildProduct(existing: Product?): Product {
        val unit = unitCombo.value.let { v ->
            if (v == "-") null else UnitOfMeasure.valueOf(v)
        }
        return Product(
            id = existing?.id ?: 1L,
            name = nameField.text.trim(),
            coordinates = Coordinates(
                x = xField.text.trim().toLong(),
                y = yField.text.trim().toFloat(),
            ),
            creationDate = existing?.creationDate ?: ZonedDateTime.now(),
            price = priceField.text.trim().toLong(),
            unitOfMeasure = unit,
            manufacturer = Organization(
                id = existing?.manufacturer?.id ?: 1L,
                name = orgNameField.text.trim(),
                fullName = orgFullNameField.text.trim(),
                employeesCount = orgEmployeesField.text.trim().toLong(),
            ),
        )
    }
}
