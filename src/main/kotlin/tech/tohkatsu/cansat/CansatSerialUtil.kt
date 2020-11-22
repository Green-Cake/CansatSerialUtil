package tech.tohkatsu.cansat

import com.fazecast.jSerialComm.SerialPort
import javafx.animation.AnimationTimer
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.stage.Stage

@ExperimentalUnsignedTypes
class CansatSerialUtil : Application() {

    companion object {

        //----- unused now...
        val dataFormat = listOf(
            DataType.FLOAT /* elapsed time */,
        )
        //-----

        const val TITLE = "Cansat Serial Utility"

        const val BAUD_RATE = 115200

        lateinit var instance: CansatSerialUtil

    }

    inner class Prompt {

        var latestFontSize = 15.0

        fun updateFontSize(size: Double) {

            val prev = latestFontSize
            latestFontSize = size
            controller.flow_prompt.children.forEach {

                if(it is Text)
                    it.font = Font.font(it.font.family, latestFontSize)

            }

        }

        fun warning(text: String) {

            controller.flow_prompt.children += Text(text).apply {
                fill = Color.DARKRED
                font = Font.font(latestFontSize)
            }

        }

        fun print(text: String) {

            controller.flow_prompt.children += Text(text).apply {
                fill = Color.GREEN
                font = Font.font(latestFontSize)
            }

        }

        fun warningln(text: String) = warning(text+"\n")

        fun println(text: String) = print(text+"\n")

    }

    val ports = mutableMapOf<String, SerialPort>()

    lateinit var stage: Stage
        private set

    lateinit var controller: MainController
        private set

    val dataList = mutableListOf<DataHolder>()

    val selectedPort get() = ports[controller.choicebox_port.value]

    var buf = mutableListOf<UByte>()

    val prompt = Prompt()

    override fun start(stage: Stage) {

        instance = this

        this.stage = stage

        stage.title = TITLE

        val loader = FXMLLoader(ClassLoader.getSystemResource("tech/tohkatsu/cansat/main.fxml"))

        stage.scene = Scene(loader.load<Pane>())

        controller = loader.getController()

        //

        stage.show()

        updatePorts()

        object : AnimationTimer() {
            override fun handle(now: Long) {
                loop()
            }
        }.start()

        prompt.println("PROGRAM STARTED...")

    }

    fun updatePorts() {

        ports.clear()
        SerialPort.getCommPorts().forEach {
            ports[it.descriptivePortName] = it
        }

        if(ports.size > 1)
            prompt.println("Found ports number: ${ports.size}")
        else
            prompt.warningln("No ports found!")

        controller.choicebox_port.items.clear()

        if(ports.size > 1) {

            controller.choicebox_port.isDisable = false
            controller.button_send.isDisable = false

            controller.choicebox_port.items.addAll(ports.keys)

        } else {

            controller.choicebox_port.isDisable = true
            controller.button_send.isDisable = true

            controller.choicebox_port.items.add("NO PORTS FOUND") // be careful!
            controller.choicebox_port.value = "NO PORTS FOUND"

        }

    }

    /**
     * called when received new data.
     */
    fun onNewData(holder: DataHolder) {

        if(holder.isCorrect)
            prompt.println("RECEIVED: $holder")
        else
            prompt.warningln("RECEIVED: $holder")

    }

    fun onSend() {

    }

    /**
     * no side effects.
     */
    fun parse(data: List<UByte>) : List<Any> {

        val result = mutableListOf<Any>()

        val d = ArrayList(data)

        while(d.isNotEmpty()) {

            val type = DataType.getByIDValue(d.removeAt(0)) ?: return emptyList()

            val bytes = mutableListOf<UByte>()

            repeat(type.bytes) {
                bytes += d.removeAt(0)
            }

            result += type.convertFromBytes(bytes)

        }

        return result

    }

    fun xorChecksum(list: List<UByte>): UByte {

        var s = byte(0u)
        list.forEach {
            s = s xor it
        }

        return s

    }

    fun byte(int: UInt) = int.toUByte()

    fun loop() {

        if(controller.slider_fontsize.value != prompt.latestFontSize)
            prompt.updateFontSize(controller.slider_fontsize.value)

        //

        val port = selectedPort ?: return

        port.baudRate = BAUD_RATE

        val stream = port.inputStream

        buf.addAll(stream.readAllBytes().asList().map { it.toUByte() })

        //ignore until A5 5A (16)
        while(buf.isEmpty() || buf.firstOrNull() != byte(0xA5u)) {
            buf.removeAt(0)
        }

        //---- parsing start

        //means it has least length of data
        while(buf.size > 6) {

            //data length that follows later
            val len = (buf[2] * 0x100u + buf[3] - 0x8000u).toInt()

            //if there is full data
            if(buf.size >= len + 6) {

                //omits the header(2bytes), length data(2bytes), checksum(1byte) and hooter(1byte)
                val actualData = buf.subList(4, 4+len).toList()

                val holder = DataHolder(parse(actualData), xorChecksum(actualData) == buf[len + 4])

                onNewData(holder)

                dataList += holder

                val footer = buf[len + 5]

                if(footer != byte(0x04u))
                    System.err.println("ILLEGAL FOOTER...")

                buf = buf.subList(len + 6, buf.size)

            }

        }

    }

}