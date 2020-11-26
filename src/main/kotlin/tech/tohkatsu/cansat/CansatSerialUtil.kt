package tech.tohkatsu.cansat

import com.fazecast.jSerialComm.SerialPort
import com.google.gson.Gson
import javafx.animation.AnimationTimer
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.*
import javafx.scene.input.KeyCode
import javafx.scene.paint.Color
import javafx.scene.shape.Box
import javafx.stage.Stage
import javafx.stage.StageStyle
import org.fxyz3d.geometry.Point3D
import org.fxyz3d.shapes.composites.PolyLine3D
import org.fxyz3d.utils.CameraTransformer
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random
import kotlin.random.nextUInt

@ExperimentalUnsignedTypes
class CansatSerialUtil : Application() {

    companion object {

        const val STYLE_NORMAL = "normal"
        const val STYLE_WARNING = "warning"

        //----- unused now...
        val dataFormat = listOf(
            DataType.FLOAT, /* elapsed time */
        )
        //-----

        const val TITLE = "Cansat Serial Utility"

        const val BAUD_RATE = 115200

        val pathOptionsFile = Paths.get("./options.json")

        val defaultConfig: Config = Config(
            "0x1a1a1aff",
            "0xffffffff",
            "0xff0000ff",
            18.0, false
        )

        lateinit var instance: CansatSerialUtil

    }

    private val gson = Gson()

    val ports = mutableMapOf<String, SerialPort>()

    lateinit var stageMain: Stage
        private set

    lateinit var stageGraph: Stage
        private set

    val graphPoints = mutableListOf<Point3D>()

    val camera = PerspectiveCamera(true)

    val cameraTransformer = CameraTransformer()

    lateinit var controllerMain: ControllerMain
        private set

    val dataList = mutableListOf<DataHolder>()

    val selectedPort get() = ports[controllerMain.choicebox_port.value]

    var buf = mutableListOf<UByte>()

    val prompt = Prompt(this)

    val doesShowAllRawData get() = controllerMain.radio_show_all_data.isSelected

    /**
     * called firstly.
     */
    override fun start(primaryStage: Stage) {

        instance = this

        this.stageMain = primaryStage

        stageMain.title = TITLE

        stageMain.setOnCloseRequest {
            saveOptions()
        }

        stageGraph = Stage(StageStyle.UTILITY).apply {
            initOwner(stageMain)
        }

        val loaderMain = FXMLLoader(ClassLoader.getSystemResource("tech/tohkatsu/cansat/main.fxml"))

        stageMain.scene = Scene(loaderMain.load())

        controllerMain = loaderMain.getController()

        stageMain.scene.setOnKeyPressed {

            when(it.code) {
                KeyCode.BACK_SLASH -> {
                    //for debug

                    prompt.println("No debug program implemented")

                }
                else -> {}
            }

        }

        //graph start

        graphPoints += Point3D(-10.0, 0.0, 0.0)
        graphPoints += Point3D(10.0, 0.0, 0.0)

        stageGraph.title = "GRAPH"

        camera.apply {

            nearClip = 0.1
            farClip = 10000.0
            fieldOfView = 50.0
            translateZ = -250.0

        }

        cameraTransformer.apply {

            children += camera
            rx.angle = -15.0

        }

        stageGraph.scene = Scene(Group(), 600.0, 600.0, true, SceneAntialiasing.BALANCED)
        stageGraph.scene.fill = Color.BLACK
        stageGraph.scene.camera = camera

        stageGraph.scene.setOnKeyPressed {

            println("pos: ${camera.translateX} ${camera.translateY} ${camera.translateZ}")

            when(it.code) {
                KeyCode.W -> camera.translateZ += 1
                KeyCode.S -> camera.translateZ -= 1
                KeyCode.D -> camera.translateX += 1
                KeyCode.A -> camera.translateX -= 1
                KeyCode.UP -> camera.translateY += 1
                KeyCode.DOWN -> camera.translateY -= 1
                else -> {}
            }

        }

        //graph end

        loadOptions()

        //

        stageMain.show()
        stageGraph.show()

        updatePorts()

        object : AnimationTimer() {
            override fun handle(now: Long) {
                loopMain()
                loopGraph()
            }
        }.start()

        prompt.println("PROGRAM STARTED")

        updateGraph()

    }

    fun updateGraph() {

        val g = Group(PolyLine3D(graphPoints, 2.0f, Color.GREEN))

        stageGraph.scene.root = g
        
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

        controllerMain.choicebox_port.items.clear()

        if(ports.size > 1) {

            controllerMain.choicebox_port.isDisable = false
            controllerMain.button_bar_send.isDisable = false

            controllerMain.choicebox_port.items.addAll(ports.keys)

        } else {

            controllerMain.choicebox_port.isDisable = true
            controllerMain.button_bar_send.isDisable = true

            controllerMain.choicebox_port.items.add("NO PORTS FOUND") // be careful!
            controllerMain.choicebox_port.value = "NO PORTS FOUND"

        }

    }

    /**
     * called when received new data.
     */
    fun onNewData(holder: DataHolder) {

        if(doesShowAllRawData || dataFormat.deepEquals(holder.data.map { it.type })) {

            if(holder.isCorrect)
                prompt.println("RECEIVED: $holder")
            else
                prompt.warningln("RECEIVED: $holder")

        }

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
    fun parse(data: List<UByte>) : List<Data> {

        val result = mutableListOf<Data>()

        val d = ArrayList(data)

        while(d.isNotEmpty()) {

            val type = DataType.getByIDValue(d.removeAt(0)) ?: return emptyList()

            val bytes = mutableListOf<UByte>()

            repeat(type.bytes) {
                bytes += d.removeAt(0)
            }

            result += Data(type, type.convertFromBytes(bytes))

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

        prompt.updateFontSize(controllerMain.slider_fontsize.value)

        prompt.updateColor(
            controllerMain.color_picker_prompt_back.value,
            controllerMain.color_picker_prompt_text.value,
            controllerMain.color_picker_prompt_warning_text.value
        )

    }

    fun loopMain() {

        updatePrompt()

        //

        val port = selectedPort ?: return

        port.baudRate = BAUD_RATE

        val stream = port.inputStream

        buf.addAll(stream.readAllBytes().asList().map { it.toUByte() })

        //ignore until A5 5A (16)
        while(buf.isNotEmpty() && buf.firstOrNull() != byte(0xA5u)) {
            buf.removeAt(0)
        }

        //---- parsing start

        //means it has least length of data
        while(buf.size > 6) {

            //[0] A5 [1] 5A

            //data seg length
            val len = ((buf[2] - 0x80u) * 0x100u + buf[3]).toInt()

            //if there is full data
            if(buf.size >= len + 6) {

                //omits the header(2bytes), length data(2bytes), checksum(1byte) and hooter(1byte)
                val actualData = buf.subList(4, 4 + len).toList()

                val holder = DataHolder(parse(actualData), xorChecksum(actualData) == buf[len + 4])

                onNewData(holder)

                dataList += holder

                val footer = buf[len + 5]

                if(footer != byte(0x04u))
                    System.err.println("ILLEGAL FOOTER...")

                buf = buf.subList(len + 6, buf.size)//remove taken data from buffer

            }

        }

    }

    fun loopGraph() {

        updateGraph()

    }

    fun loadOptions() {

        val config: Config = try {
            gson.fromJson(Files.newBufferedReader(pathOptionsFile), Config::class.java)
        } catch (e: Throwable) {
            defaultConfig
        }

        controllerMain.applyConfig(config)

    }

    fun saveOptions() {

        val config = Config(
                controllerMain.color_picker_prompt_back.value.toString(),
                controllerMain.color_picker_prompt_text.value.toString(),
                controllerMain.color_picker_prompt_warning_text.value.toString(),
                controllerMain.slider_fontsize.value,
                controllerMain.radio_show_all_data.isSelected
        )

        if(Files.notExists(pathOptionsFile))
            Files.createFile(pathOptionsFile)

        Files.newOutputStream(pathOptionsFile, StandardOpenOption.WRITE).use {
            it.write(gson.toJson(config).toByteArray())
        }

    }

}