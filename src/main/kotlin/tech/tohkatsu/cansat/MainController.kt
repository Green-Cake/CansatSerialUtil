package tech.tohkatsu.cansat

import com.fazecast.jSerialComm.SerialPort
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.ChoiceBox
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import java.net.URL
import java.util.*

class MainController : Initializable {

    @FXML
    lateinit var area_prompt: TextArea

    @FXML
    lateinit var field_px: TextField

    @FXML
    lateinit var field_py: TextField

    @FXML
    lateinit var field_pz: TextField

    @FXML
    lateinit var field_send: TextField

    @FXML
    lateinit var button_send: Button

    @FXML
    lateinit var choicebox_port: ChoiceBox<String>

    @FXML
    lateinit var button_update_port: Button

    @FXML
    fun onUpdatePorts() {
        CansatSerialUtil.instance.updatePorts()
    }

    @FXML
    fun onSend() {



    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {



    }
}