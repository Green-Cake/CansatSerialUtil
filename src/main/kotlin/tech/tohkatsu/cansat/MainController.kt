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
    lateinit var button_bar_send: ButtonBar

    @FXML
    lateinit var button_send_alpha: Button

    @FXML
    lateinit var button_send_beta: Button

    @FXML
    lateinit var choicebox_port: ChoiceBox<String>

    @FXML
    lateinit var button_update_port: Button

    //option start

    @FXML
    lateinit var slider_fontsize: Slider

    @FXML
    lateinit var color_picker_prompt_back: ColorPicker


    @FXML
    @ExperimentalUnsignedTypes
    fun onUpdatePorts() {
        CansatSerialUtil.instance.updatePorts()
    }

    @FXML
    @ExperimentalUnsignedTypes
    fun onSendAlpha() {
        CansatSerialUtil.instance.sendTo(Address.ALPHA, field_send.text)
    }

    @FXML
    @ExperimentalUnsignedTypes
    fun onSendBeta() {
        CansatSerialUtil.instance.sendTo(Address.BETA, field_send.text)
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {



    }
}