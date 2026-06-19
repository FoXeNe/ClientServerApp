package gui.view

import gui.AppState
import gui.LocaleManager
import gui.MainApp
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import model.CommandType
import model.Request

class LoginView : VBox(12.0) {
    init {
        padding = Insets(20.0)
        alignment = Pos.CENTER
        minWidth = 420.0

        val langBar = buildLangBar()
        val tabPane = TabPane()
        tabPane.tabs.addAll(buildLoginTab(), buildRegisterTab())

        children.addAll(langBar, tabPane)
    }

    private fun buildLangBar(): HBox {
        val label = Label(LocaleManager.getString("menu.language") + ":")
        val combo = ComboBox<String>()
        LocaleManager.availableLocales.forEach { (_, name) -> combo.items.add(name) }
        combo.value = LocaleManager.availableLocales.find { it.first == LocaleManager.locale }?.second
            ?: LocaleManager.availableLocales[0].second
        combo.setOnAction {
            val idx = combo.selectionModel.selectedIndex
            if (idx >= 0) {
                LocaleManager.setLocale(LocaleManager.availableLocales[idx].first)
                MainApp.navigateToLogin()
            }
        }
        return HBox(8.0, label, combo).apply { alignment = Pos.CENTER_RIGHT }
    }

    private fun buildLoginTab(): Tab {
        val userField = TextField()
        val passField = PasswordField()
        val btn = Button(LocaleManager.getString("btn.login"))
        val status = Label().apply { style = "-fx-text-fill: red;" }

        val grid = GridPane().apply {
            hgap = 10.0; vgap = 10.0; padding = Insets(20.0)
            add(Label(LocaleManager.getString("field.username")), 0, 0)
            add(userField.also { it.prefWidth = 200.0 }, 1, 0)
            add(Label(LocaleManager.getString("field.password")), 0, 1)
            add(passField, 1, 1)
            add(btn, 1, 2)
            add(status, 0, 3, 2, 1)
        }

        btn.setOnAction {
            val login = userField.text.trim()
            val pass = passField.text
            if (login.isBlank() || pass.isBlank()) {
                status.text = LocaleManager.getString("msg.no_selection"); return@setOnAction
            }
            val response = AppState.network.sendRaw(Request(CommandType.LOGIN, login = login, password = pass))
            if (response == null) {
                status.text = LocaleManager.getString("msg.server_unavailable")
            } else if (response.success) {
                AppState.network.setCredentials(login, pass)
                AppState.currentUser = login
                MainApp.navigateToMain()
            } else {
                status.text = response.message
            }
        }

        return Tab(LocaleManager.getString("login.tab"), grid).also { it.isClosable = false }
    }

    private fun buildRegisterTab(): Tab {
        val userField = TextField()
        val passField = PasswordField()
        val btn = Button(LocaleManager.getString("btn.register"))
        val status = Label().apply { style = "-fx-text-fill: red;" }

        val grid = GridPane().apply {
            hgap = 10.0; vgap = 10.0; padding = Insets(20.0)
            add(Label(LocaleManager.getString("field.username")), 0, 0)
            add(userField.also { it.prefWidth = 200.0 }, 1, 0)
            add(Label(LocaleManager.getString("field.password")), 0, 1)
            add(passField, 1, 1)
            add(btn, 1, 2)
            add(status, 0, 3, 2, 1)
        }

        btn.setOnAction {
            val login = userField.text.trim()
            val pass = passField.text
            if (login.isBlank() || pass.isBlank()) {
                status.text = LocaleManager.getString("msg.no_selection"); return@setOnAction
            }
            val response = AppState.network.sendRaw(Request(CommandType.REGISTER, login = login, password = pass))
            if (response == null) {
                status.text = LocaleManager.getString("msg.server_unavailable")
            } else if (response.success) {
                AppState.network.setCredentials(login, pass)
                AppState.currentUser = login
                MainApp.navigateToMain()
            } else {
                status.text = response.message
            }
        }

        return Tab(LocaleManager.getString("register.tab"), grid).also { it.isClosable = false }
    }
}
