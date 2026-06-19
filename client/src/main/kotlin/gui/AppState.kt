package gui

import gui.view.MainView
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.stage.Stage
import manager.NetworkManager
import model.Product
import java.util.concurrent.ScheduledFuture

object AppState {
    lateinit var stage: Stage
    lateinit var network: NetworkManager
    var currentUser: String = ""
    val products: ObservableList<Product> = FXCollections.observableArrayList()
    var poller: ScheduledFuture<*>? = null
    var mainView: MainView? = null
}
