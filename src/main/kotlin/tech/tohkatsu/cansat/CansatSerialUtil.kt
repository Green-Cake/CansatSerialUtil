package tech.tohkatsu.cansat

import com.fazecast.jSerialComm.SerialPort
import javafx.animation.AnimationTimer
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.stage.Stage
import java.util.*
import kotlin.collections.ArrayList

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
        var latestNormalFill = Color.BLACK

        private fun prefixInfo(calendar: Calendar = Calendar.getInstance()) =
            "[${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)}:${calendar.get(Calendar.SECOND)}:${calendar.get(Calendar.MILLISECOND)}] "

        fun updateFontSize(size: Double) {

            latestFontSize = size
            controller.flow_prompt.children.filterIsInstance<Text>().forEach {

                it.font = Font.font(it.font.family, latestFontSize)

            }

        }

        fun updateNormalFill(fill: Color) {

            latestNormalFill = fill
            controller.flow_prompt.children.filterIsInstance<Text>().forEach {
                    it.fill = latestNormalFill

            }

        }

        fun print(text: String, color: Color) {

            controller.flow_prompt.children += Text(prefixInfo()+text).apply {
                fill = color
                font = Font.font(latestFontSize)
            }

        }

        fun warning(text: String) = print(text, Color.RED)

        fun print(text: String) = print(text, latestNormalFill)

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
            prompt.warningln("No ports found! press \"Update Ports\" button to recheck available ports.")

        controller.choicebox_port.items.clear()

        if(ports.size > 1) {

            controller.choicebox_port.isDisable = false
            controller.button_bar_send.isDisable = false

            controller.choicebox_port.items.addAll(ports.keys)

        } else {

            controller.choicebox_port.isDisable = true
            controller.button_bar_send.isDisable = true

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

    fun sendTo(address: Address, raw: String) {

        val data = mutableListOf<UByte>()

        raw.split(',').forEach {

            val trimmed = it.trim()

            data += try {
                if(trimmed.startsWith("0x"))
                    trimmed.drop(2).toUByte(16)
                else
                    trimmed.toUByte()
            } catch (e: Throwable) {
                return@forEach
            }

        }

        sendTo(address, data)

    }

    fun sendTo(address: Address, data: List<UByte>) {

        if(selectedPort == null)
            return

        if(data.size > 0xFF) {
            prompt.warningln("Can't send it! it's too large to send. It must be lower than 256")
            return
        }

        val q = mutableListOf(address.value, byte(0xA0u), byte(0x34u), byte(0x07u), byte(0xFFu))

        q.addAll(data)

        val checksum = xorChecksum(q)

        val r = mutableListOf(byte(0xA5u), byte(0x5Au), byte(0x80u))

        r += q.size.toUByte()

        r.addAll(q)

        r += checksum

        selectedPort!!.writeBytes(r.map { it.toByte() }.toByteArray(), r.size.toLong())

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

        controller.flow_prompt.background = Background(BackgroundFill(controller.color_picker_prompt_back.value, CornerRadii.EMPTY, Insets.EMPTY))

        prompt.updateNormalFill(controller.color_picker_prompt_back.value.invert())

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