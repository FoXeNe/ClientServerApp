package gui

import gui.view.LoginView
import gui.view.MainView
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.stage.Stage
import manager.NetworkManager

class MainApp : Application() {
    override fun start(stage: Stage) {
        AppState.stage = stage
        val host = System.getenv("SERVER_HOST") ?: "localhost"
        val port = System.getenv("SERVER_PORT")?.toIntOrNull() ?: 45205
        AppState.network = NetworkManager(host, port)
        navigateToLogin()
    }

    override fun stop() {
        AppState.poller?.cancel(false)
    }

    companion object {
        fun navigateToLogin() {
            AppState.poller?.cancel(false)
            AppState.poller = null
            AppState.mainView?.dispose()
            AppState.mainView = null
            AppState.products.clear()
            AppState.network.resetConnection()
            val scene = Scene(LoginView(), 460.0, 400.0)
            AppState.stage.scene = scene
            AppState.stage.title = LocaleManager.getString("window.title")
            AppState.stage.show()
        }

        fun navigateToMain() {
            AppState.poller?.cancel(false)
            AppState.poller = null
            AppState.mainView?.dispose()
            val view = MainView()
            AppState.mainView = view
            val scene = Scene(view, 1300.0, 850.0)
            AppState.stage.scene = scene
            AppState.stage.title = LocaleManager.getString("window.main.title")
            view.startPolling()
        }

        fun showAlert(
            msg: String,
            type: Alert.AlertType = Alert.AlertType.INFORMATION,
            title: String? = null,
        ) {
            Platform.runLater {
                val alert = Alert(type)
                alert.title = title ?: when (type) {
                    Alert.AlertType.ERROR -> LocaleManager.getString("msg.error")
                    else -> LocaleManager.getString("info.title")
                }
                alert.headerText = null
                alert.contentText = msg
                alert.showAndWait()
            }
        }

        fun confirm(msg: String): Boolean {
            val alert = Alert(Alert.AlertType.CONFIRMATION)
            alert.title = LocaleManager.getString("confirm.title")
            alert.headerText = null
            alert.contentText = msg
            val result = alert.showAndWait()
            return result.isPresent && result.get().buttonData.isDefaultButton
        }
    }
}
