package gui.view

import gui.AppState
import gui.LocaleManager
import gui.MainApp
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.*
import io.IOHandler
import io.IOWrapper
import model.CommandType
import model.Product
import model.Request
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MainView : BorderPane() {
    private val tableView = ProductTableView()
    private val canvasView = CanvasView { product -> editProduct(product) }
    private val statusLabel = Label()
    private val scheduler = Executors.newSingleThreadScheduledExecutor { r ->
        Thread(r, "poller").also { it.isDaemon = true }
    }
    private val localeListener: () -> Unit = { Platform.runLater { rebuildLocale() } }

    private val cmdIO: IOWrapper by lazy {
        IOWrapper(object : IOHandler {
            override fun println(message: String) {}
            override fun readLine(): String? = null
        })
    }
    private val cm: manager.CommandManager by lazy { buildCommandManager() }

    private fun buildCommandManager(): manager.CommandManager {
        val cm = manager.CommandManager()
        cm.register(command.commands.Add(cmdIO, AppState.network))
        cm.register(command.commands.AddIfMin(cmdIO, AppState.network))
        cm.register(command.commands.Update(cmdIO, AppState.network))
        cm.register(command.commands.Clear(AppState.network))
        cm.register(command.commands.FilterByManufacturer(AppState.network))
        cm.register(command.commands.FilterGreaterThanManufacturer(AppState.network))
        cm.register(command.commands.Info(AppState.network))
        cm.register(command.commands.RemoveById(AppState.network))
        cm.register(command.commands.RemoveFirst(AppState.network))
        cm.register(command.commands.Show(AppState.network))
        cm.register(command.commands.SumOfPrice(AppState.network))
        cm.register(command.commands.Help(cm))
        cm.register(command.commands.History(cm))
        cm.register(command.commands.ExecuteScript(cmdIO, cm))
        return cm
    }

    private fun primeIO(lines: List<String>) {
        val queue = ArrayDeque(lines)
        cmdIO.swapHandler(object : IOHandler {
            override fun println(message: String) {}
            override fun readLine(): String? = queue.removeFirstOrNull()
        })
    }

    private fun resetIO() {
        cmdIO.swapHandler(object : IOHandler {
            override fun println(message: String) {}
            override fun readLine(): String? = null
        })
    }

    private fun productToLines(product: Product): List<String> = listOf(
        product.name,
        product.coordinates.x.toString(),
        product.coordinates.y.toString(),
        product.price.toString(),
        product.unitOfMeasure?.name ?: "",
        product.manufacturer.name,
        product.manufacturer.fullName,
        product.manufacturer.employeesCount.toString(),
    )

    init {
        top = buildTop()
        center = buildCenter()
        bottom = buildStatus()

        LocaleManager.addListener(localeListener)

        refresh()
    }

    fun dispose() {
        LocaleManager.removeListener(localeListener)
        canvasView.stop()
        scheduler.shutdownNow()
    }

    private fun buildTop(): VBox {
        val menuBar = buildMenuBar()
        val userBar = buildUserBar()
        return VBox(menuBar, userBar)
    }

    private fun buildUserBar(): HBox {
        val userLabel = Label(LocaleManager.format("user.label", AppState.currentUser))
        userLabel.style = "-fx-font-weight: bold; -fx-padding: 4 8;"

        val logoutBtn = Button(LocaleManager.getString("btn.logout"))
        logoutBtn.setOnAction {
            AppState.currentUser = ""
            MainApp.navigateToLogin()
        }

        val spacer = Region().also { HBox.setHgrow(it, Priority.ALWAYS) }
        return HBox(8.0, userLabel, spacer, logoutBtn).apply {
            padding = Insets(4.0, 8.0, 4.0, 8.0)
            alignment = Pos.CENTER_LEFT
            style = "-fx-background-color: #e8eaf6;"
        }
    }

    private fun buildMenuBar(): MenuBar {
        val cmdMenu = Menu(LocaleManager.getString("menu.commands"))

        fun item(key: String, action: () -> Unit): MenuItem {
            val mi = MenuItem(LocaleManager.getString(key))
            mi.setOnAction { action() }
            return mi
        }

        cmdMenu.items.addAll(
            item("cmd.add") { addProduct() },
            item("cmd.add_if_min") { addIfMin() },
            SeparatorMenuItem(),
            item("cmd.update") { updateSelected() },
            item("cmd.remove") { removeSelected() },
            item("cmd.remove_by_id") { removeById() },
            item("cmd.remove_first") { removeFirst() },
            SeparatorMenuItem(),
            item("cmd.clear") { clearCollection() },
            SeparatorMenuItem(),
            item("cmd.filter_by_manufacturer") { filterByManufacturer() },
            item("cmd.filter_greater") { filterGreater() },
            item("cmd.info") { showInfo() },
            item("cmd.sum_of_price") { sumOfPrice() },
            SeparatorMenuItem(),
            item("cmd.show") { refresh() },
            item("cmd.help") { showHelp() },
            item("cmd.history") { showHistory() },
            item("cmd.execute_script") { executeScript() },
        )

        val currentLocaleName = LocaleManager.availableLocales.find { it.first == LocaleManager.locale }?.second
            ?: LocaleManager.availableLocales[0].second
        val langMenu = Menu(LocaleManager.getString("menu.language") + ": $currentLocaleName")
        LocaleManager.availableLocales.forEach { (code, name) ->
            val mi = MenuItem(name)
            mi.setOnAction { LocaleManager.setLocale(code) }
            langMenu.items.add(mi)
        }

        val menuBar = MenuBar(cmdMenu, langMenu)
        menuBar.useSystemMenuBarProperty().set(false)
        return menuBar
    }

    private fun buildCenter(): SplitPane {
        val tabPane = TabPane()
        val tableTab = Tab(LocaleManager.getString("tab.table"), tableView)
        val canvasTab = Tab(LocaleManager.getString("tab.canvas"), canvasView)
        tableTab.isClosable = false
        canvasTab.isClosable = false
        tabPane.tabs.addAll(tableTab, canvasTab)

        val toolBar = buildToolBar()
        val tableBox = VBox(toolBar, tabPane).also { VBox.setVgrow(tabPane, Priority.ALWAYS) }

        return SplitPane(tableBox).also { it.setDividerPositions(1.0) }
    }

    private fun buildToolBar(): ToolBar {
        fun btn(key: String, action: () -> Unit): Button =
            Button(LocaleManager.getString(key)).also { it.setOnAction { action() } }

        return ToolBar(
            btn("cmd.add") { addProduct() },
            btn("cmd.update") { updateSelected() },
            btn("cmd.remove") { removeSelected() },
            Separator(),
            btn("cmd.show") { refresh() },
            btn("cmd.info") { showInfo() },
            btn("cmd.sum_of_price") { sumOfPrice() },
        )
    }

    private fun buildStatus(): HBox {
        val box = HBox(statusLabel)
        box.padding = Insets(4.0, 8.0, 4.0, 8.0)
        box.style = "-fx-background-color: #f0f0f0;"
        return box
    }

    fun startPolling() {
        val future = scheduler.scheduleWithFixedDelay({
            runCatching {
                val response = AppState.network.sendRequest(Request(CommandType.SHOW))
                if (response?.success == true) {
                    Platform.runLater {
                        AppState.products.setAll(response.collection ?: emptyList())
                        canvasView.updateAnimations()
                        tableView.applyFilterSort()
                        statusLabel.text = response.message
                    }
                }
            }
        }, 0L, 2L, TimeUnit.SECONDS)
        AppState.poller = future
    }

    private fun refresh() {
        Thread {
            val result = cm.initCommand("show")
            Platform.runLater {
                AppState.products.setAll(result.collection ?: emptyList())
                canvasView.updateAnimations()
                tableView.applyFilterSort()
                status(result.message)
            }
        }.also { it.isDaemon = true }.start()
    }

    private fun addProduct() {
        val product = ProductDialog().showAndWait().orElse(null) ?: return
        primeIO(productToLines(product))
        val result = cm.initCommand("add")
        resetIO()
        status(result.message)
        refresh()
    }

    private fun addIfMin() {
        val product = ProductDialog().showAndWait().orElse(null) ?: return
        primeIO(productToLines(product))
        val result = cm.initCommand("add_if_min")
        resetIO()
        status(result.message)
        refresh()
    }

    private fun updateSelected() {
        val selected = tableView.getSelectedProduct()
        if (selected == null) { status(LocaleManager.getString("msg.no_selection")); return }
        editProduct(selected)
    }

    fun editProduct(product: Product) {
        if (product.owner != null && product.owner != AppState.currentUser) {
            status(LocaleManager.getString("msg.not_owner")); return
        }
        val updated = ProductDialog(product).showAndWait().orElse(null) ?: return
        primeIO(productToLines(updated))
        val result = cm.initCommand("update ${product.id}")
        resetIO()
        status(result.message)
        refresh()
    }

    private fun removeSelected() {
        val selected = tableView.getSelectedProduct()
        if (selected == null) { status(LocaleManager.getString("msg.no_selection")); return }
        if (!MainApp.confirm(LocaleManager.getString("msg.confirm_delete"))) return
        val result = cm.initCommand("remove_by_id ${selected.id}")
        status(result.message)
        refresh()
    }

    private fun removeById() {
        val dialog = TextInputDialog()
        dialog.title = LocaleManager.getString("cmd.remove_by_id")
        dialog.headerText = null
        dialog.contentText = LocaleManager.getString("msg.enter_id")
        val id = dialog.showAndWait().orElse(null)?.trim() ?: return
        val result = cm.initCommand("remove_by_id $id")
        status(result.message)
        if (result.success) refresh()
    }

    private fun removeFirst() {
        if (!MainApp.confirm(LocaleManager.getString("msg.confirm_delete"))) return
        val result = cm.initCommand("remove_first")
        status(result.message)
        refresh()
    }

    private fun clearCollection() {
        if (!MainApp.confirm(LocaleManager.getString("msg.confirm_clear"))) return
        val result = cm.initCommand("clear")
        status(result.message)
        refresh()
    }

    private fun filterByManufacturer() {
        val dialog = TextInputDialog()
        dialog.title = LocaleManager.getString("cmd.filter_by_manufacturer")
        dialog.headerText = null
        dialog.contentText = LocaleManager.getString("msg.enter_manufacturer")
        val name = dialog.showAndWait().orElse(null) ?: return
        val result = cm.initCommand("filter_by_manufacturer $name")
        status(result.message)
        Platform.runLater {
            AppState.products.setAll(result.collection ?: emptyList())
            canvasView.updateAnimations()
            tableView.applyFilterSort()
        }
    }

    private fun filterGreater() {
        val dialog = TextInputDialog()
        dialog.title = LocaleManager.getString("cmd.filter_greater")
        dialog.headerText = null
        dialog.contentText = LocaleManager.getString("msg.enter_manufacturer")
        val name = dialog.showAndWait().orElse(null) ?: return
        val result = cm.initCommand("filter_greater_than_manufacturer $name")
        status(result.message)
        Platform.runLater {
            AppState.products.setAll(result.collection ?: emptyList())
            canvasView.updateAnimations()
            tableView.applyFilterSort()
        }
    }

    private fun showInfo() {
        val result = cm.initCommand("info")
        MainApp.showAlert(result.message)
    }

    private fun sumOfPrice() {
        val result = cm.initCommand("sum_of_price")
        val formatted = LocaleManager.getNumberFormat().format(
            result.message.filter { it.isDigit() }.toLongOrNull() ?: 0L
        )
        MainApp.showAlert("${LocaleManager.getString("cmd.sum_of_price")}: $formatted")
    }

    private fun showHelp() {
        val result = cm.initCommand("help")
        MainApp.showAlert(result.message)
    }

    private fun showHistory() {
        val result = cm.initCommand("history")
        MainApp.showAlert(result.message)
    }

    private fun executeScript(path: String? = null) {
        val filePath = path ?: run {
            val chooser = javafx.stage.FileChooser()
            chooser.title = LocaleManager.getString("msg.select_script")
            chooser.showOpenDialog(AppState.stage)?.absolutePath ?: return
        }
        val result = cm.initCommand("execute_script $filePath")
        refresh()
        MainApp.showAlert(result.message)
    }

    private fun rebuildLocale() {
        MainApp.navigateToMain()
    }

    private fun status(msg: String) {
        Platform.runLater { statusLabel.text = msg }
    }
}
