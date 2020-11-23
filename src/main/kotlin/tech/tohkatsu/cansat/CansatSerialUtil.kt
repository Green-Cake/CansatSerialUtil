package tech.tohkatsu.cansat

import com.fazecast.jSerialComm.SerialPort
import com.google.gson.Gson
import javafx.animation.AnimationTimer
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.stage.Stage
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import kotlin.collections.ArrayList

@ExperimentalUnsignedTypes
class CansatSerialUtil : Application() {

    companion object {

        const val STYLE_NORMAL = "normal"
        const val STYLE_WARNING = "warning"

        //----- unused now...
        val dataFormat = listOf(
            DataType.FLOAT /* elapsed time */,
        )
        //-----

        const val TITLE = "Cansat Serial Utility"

        const val BAUD_RATE = 115200

        val pathOptionsFile = Paths.get("./options.json")

        val defaultConfig: Config = Config(
            "0x1a1a1aff",
            "0xffffffff",
            "0xff0000ff",
            18.0
        )

        lateinit var instance: CansatSerialUtil

    }

    private val gson = Gson()

    val ports = mutableMapOf<String, SerialPort>()

    lateinit var stage: Stage
        private set

    lateinit var controller: MainController
        private set

    val dataList = mutableListOf<DataHolder>()

    val selectedPort get() = ports[controller.choicebox_port.value]

    var buf = mutableListOf<UByte>()

    val prompt = Prompt(this)

    override fun start(stage: Stage) {

        instance = this

        this.stage = stage

        stage.title = TITLE

        stage.setOnCloseRequest {

            saveOptions()

        }

        val loader = FXMLLoader(ClassLoader.getSystemResource("tech/tohkatsu/cansat/main.fxml"))

        stage.scene = Scene(loader.load<Pane>())

        controller = loader.getController()

        loadOptions()

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

    fun updatePrompt() {
        prompt.updateFontSize(controller.slider_fontsize.value)

        prompt.updateColor(
            controller.color_picker_prompt_back.value,
            controller.color_picker_prompt_text.value,
            controller.color_picker_prompt_warning_text.value
        )
    }

    fun loop() {

        updatePrompt()

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

    fun loadOptions() {

        val config: Config = try {
            gson.fromJson(Files.newBufferedReader(pathOptionsFile), Config::class.java)
        } catch (e: Throwable) {
            defaultConfig
        }

        controller.applyConfig(config)

    }

    fun saveOptions() {

        val config = Config(
            controller.color_picker_prompt_back.value.toString(),
            controller.color_picker_prompt_text.value.toString(),
            controller.color_picker_prompt_warning_text.value.toString(),
            controller.slider_fontsize.value
        )

        if(Files.notExists(pathOptionsFile))
            Files.createFile(pathOptionsFile)

        Files.newOutputStream(pathOptionsFile, StandardOpenOption.WRITE).use {
            it.write(gson.toJson(config).toByteArray())
        }

    }

}