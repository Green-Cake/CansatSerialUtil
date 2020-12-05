package tech.tohkatsu.cansat

import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.paint.Color
import javafx.scene.text.TextFlow
import tech.tohkatsu.cansat.util.Address
import java.net.URL
import java.util.*

@ExperimentalUnsignedTypes
class ControllerMain : Initializable {

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
    lateinit var color_picker_prompt_text: ColorPicker

    @FXML
    lateinit var color_picker_prompt_warning_text: ColorPicker

    @FXML
    lateinit var button_reset: Button

    @FXML
    lateinit var radio_show_all_data: RadioButton

    @FXML
    fun onUpdatePorts() {
        CansatSerialUtil.instance.updatePorts()
    }

    @FXML
    fun onSendAlpha() {
        CansatSerialUtil.instance.sendTo(Address.ALPHA, field_send.text)
    }

    @FXML
    fun onSendBeta() {
        CansatSerialUtil.instance.sendTo(Address.BETA, field_send.text)
    }

    @FXML
    fun onReset() {
        applyConfig(CansatSerialUtil.defaultConfig)
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {

        color_picker_prompt_back.value = Color.grayRgb(0x1A)
        color_picker_prompt_text.value = Color.WHITE
        color_picker_prompt_warning_text.value = Color.RED
        slider_fontsize.value = 18.0
        radio_show_all_data.isSelected = false

    }

    fun applyConfig(config: Config) {

        color_picker_prompt_back.value = Color.valueOf(config.colorPromptBackground)
        color_picker_prompt_text.value = Color.valueOf(config.colorPromptText)
        color_picker_prompt_warning_text.value = Color.valueOf(config.colorPromptWarningText)
        slider_fontsize.value = config.font_size
        radio_show_all_data.isSelected = config.doesShowAllRawData

        CansatSerialUtil.instance.updatePrompt()

    }

}