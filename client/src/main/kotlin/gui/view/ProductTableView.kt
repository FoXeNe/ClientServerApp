package gui.view

import gui.AppState
import gui.LocaleManager
import javafx.collections.FXCollections
import javafx.collections.transformation.FilteredList
import javafx.collections.transformation.SortedList
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import model.Product
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ProductTableView : VBox(4.0) {
    private val filteredProducts = FilteredList(AppState.products)
    private val sortedProducts = SortedList(filteredProducts)
    val table = TableView(sortedProducts)

    private data class ColDef(val key: String, val label: String) {
        override fun toString() = label
    }

    private val fmt = LocaleManager.getNumberFormat()
    private val dateFmt = LocaleManager.getDateFormat()
    private val filterDateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault())

    private val colDefs = listOf(
        "col.id", "col.name", "col.x", "col.y", "col.price",
        "col.unit", "col.org_name", "col.org_fullname", "col.org_employees",
        "col.date", "col.owner",
    ).map { ColDef(it, LocaleManager.getString(it)) }

    private val filterColBox = ComboBox(FXCollections.observableArrayList(colDefs))
    private val filterField = TextField()
    private val clearBtn = Button("✕")

    private val tableColumns = mutableMapOf<String, TableColumn<Product, *>>()

    init {
        padding = Insets(4.0)

        filterColBox.selectionModel.selectFirst()
        filterColBox.prefWidth = 130.0
        filterField.prefWidth = 200.0
        clearBtn.setOnAction { filterField.clear() }

        updateFilterPlaceholder(colDefs.first())
        filterColBox.valueProperty().addListener { _, _, col ->
            updateFilterPlaceholder(col)
            applyFilter()
        }

        val controlBar = HBox(6.0,
            Label("Фильтр:"), filterColBox, filterField, clearBtn,
        ).apply { padding = Insets(2.0); alignment = Pos.CENTER_LEFT }

        sortedProducts.comparatorProperty().bind(table.comparatorProperty())

        setupTable()

        filterField.textProperty().addListener { _, _, _ -> applyFilter() }

        children.addAll(controlBar, table)
        VBox.setVgrow(table, Priority.ALWAYS)
    }

    private fun updateFilterPlaceholder(col: ColDef?) {
        filterField.promptText = when (col?.key) {
            "col.date" -> "гггг-мм-дд чч:мм"
            else -> LocaleManager.getString("filter.placeholder")
        }
    }

    private fun productValue(p: Product, key: String): String = when (key) {
        "col.id"           -> fmt.format(p.id)
        "col.name"         -> p.name
        "col.x"            -> fmt.format(p.coordinates.x)
        "col.y"            -> fmt.format(p.coordinates.y)
        "col.price"        -> fmt.format(p.price)
        "col.unit"         -> p.unitOfMeasure?.name ?: "-"
        "col.org_name"     -> p.manufacturer.name
        "col.org_fullname" -> p.manufacturer.fullName
        "col.org_employees"-> fmt.format(p.manufacturer.employeesCount)
        "col.date"         -> filterDateFmt.format(p.creationDate.toInstant())
        "col.owner"        -> p.owner ?: ""
        else               -> ""
    }

    private fun setupTable() {
        fun strCol(key: String, getter: (Product) -> String): TableColumn<Product, String> {
            val c = TableColumn<Product, String>(LocaleManager.getString(key))
            c.setCellValueFactory { javafx.beans.property.SimpleStringProperty(getter(it.value)) }
            tableColumns[key] = c
            return c
        }

        val idCol = TableColumn<Product, Long>(LocaleManager.getString("col.id")).also { c ->
            c.setCellValueFactory { javafx.beans.property.SimpleObjectProperty(it.value.id) }
            c.setCellFactory { _ -> object : TableCell<Product, Long>() {
                override fun updateItem(v: Long?, empty: Boolean) { super.updateItem(v, empty); text = if (empty || v == null) null else fmt.format(v) }
            }}
            tableColumns["col.id"] = c
        }

        val xCol = TableColumn<Product, Long>(LocaleManager.getString("col.x")).also { c ->
            c.setCellValueFactory { javafx.beans.property.SimpleObjectProperty(it.value.coordinates.x) }
            c.setCellFactory { _ -> object : TableCell<Product, Long>() {
                override fun updateItem(v: Long?, empty: Boolean) { super.updateItem(v, empty); text = if (empty || v == null) null else fmt.format(v) }
            }}
            tableColumns["col.x"] = c
        }

        val yCol = TableColumn<Product, Float>(LocaleManager.getString("col.y")).also { c ->
            c.setCellValueFactory { javafx.beans.property.SimpleObjectProperty(it.value.coordinates.y) }
            c.setCellFactory { _ -> object : TableCell<Product, Float>() {
                override fun updateItem(v: Float?, empty: Boolean) { super.updateItem(v, empty); text = if (empty || v == null) null else fmt.format(v) }
            }}
            tableColumns["col.y"] = c
        }

        val priceCol = TableColumn<Product, Long>(LocaleManager.getString("col.price")).also { c ->
            c.setCellValueFactory { javafx.beans.property.SimpleObjectProperty(it.value.price) }
            c.setCellFactory { _ -> object : TableCell<Product, Long>() {
                override fun updateItem(v: Long?, empty: Boolean) { super.updateItem(v, empty); text = if (empty || v == null) null else fmt.format(v) }
            }}
            tableColumns["col.price"] = c
        }

        val empCol = TableColumn<Product, Long>(LocaleManager.getString("col.org_employees")).also { c ->
            c.setCellValueFactory { javafx.beans.property.SimpleObjectProperty(it.value.manufacturer.employeesCount) }
            c.setCellFactory { _ -> object : TableCell<Product, Long>() {
                override fun updateItem(v: Long?, empty: Boolean) { super.updateItem(v, empty); text = if (empty || v == null) null else fmt.format(v) }
            }}
            tableColumns["col.org_employees"] = c
        }

        val dateCol = TableColumn<Product, java.time.ZonedDateTime>(LocaleManager.getString("col.date")).also { c ->
            c.setCellValueFactory { javafx.beans.property.SimpleObjectProperty(it.value.creationDate) }
            c.setCellFactory { _ -> object : TableCell<Product, java.time.ZonedDateTime>() {
                override fun updateItem(v: java.time.ZonedDateTime?, empty: Boolean) {
                    super.updateItem(v, empty)
                    text = if (empty || v == null) null else dateFmt.format(java.util.Date.from(v.toInstant()))
                }
            }}
            tableColumns["col.date"] = c
        }

        @Suppress("UNCHECKED_CAST")
        table.columns.addAll(
            idCol as TableColumn<Product, *>,
            strCol("col.name") { it.name },
            xCol, yCol, priceCol,
            strCol("col.unit") { it.unitOfMeasure?.name ?: "-" },
            strCol("col.org_name") { it.manufacturer.name },
            strCol("col.org_fullname") { it.manufacturer.fullName },
            empCol, dateCol,
            strCol("col.owner") { it.owner ?: "" },
        )
        table.columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
    }

    fun applyFilter() {
        val key = filterColBox.value?.key
        val text = filterField.text.trim().lowercase()

        filteredProducts.setPredicate { p ->
            if (text.isEmpty() || key == null) true
            else productValue(p, key).lowercase().contains(text)
        }
    }

    fun applyFilterSort() = applyFilter()

    fun getSelectedProduct(): Product? = table.selectionModel.selectedItem
}
