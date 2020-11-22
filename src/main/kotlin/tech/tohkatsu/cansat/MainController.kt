package tech.tohkatsu.cansat

import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.text.TextFlow
import java.net.URL
import java.util.*

class MainController : Initializable {

    @FXML
    lateinit var scrollpane_prompt: ScrollPane

    @FXML
    lateinit var flow_prompt: TextFlow

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

    //option start

    @FXML
    lateinit var slider_fontsize: Slider


    @FXML
    @ExperimentalUnsignedTypes
    fun onUpdatePorts() {
        CansatSerialUtil.instance.updatePorts()
    }

    @FXML
    fun onSend() {



    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {



    }
}