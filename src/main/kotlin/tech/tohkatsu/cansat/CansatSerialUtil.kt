package tech.tohkatsu.cansat

import com.fazecast.jSerialComm.SerialPort
import javafx.animation.AnimationTimer
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.stage.Stage
import javafx.util.StringConverter

class CansatSerialUtil : Application() {

    companion object {

        const val TITLE = "Cansat Serial Utility"

        const val BAUD_RATE = 115200

        lateinit var instance: CansatSerialUtil

    }

    val ports = mutableMapOf<String, SerialPort>()

    lateinit var stage: Stage
        private set

    lateinit var controller: MainController
        private set

    val selectedPort get() = ports[controller.choicebox_port.value]

    val buf = mutableListOf<Byte>()

    override fun start(stage: Stage) {

        instance = this

        this.stage = stage

        stage.title = TITLE

        val loader = FXMLLoader(ClassLoader.getSystemResource("tech/tohkatsu/cansat/main.fxml"))

        stage.scene = Scene(loader.load<Pane>())

        controller = loader.getController()

        stage.show()

        updatePorts()

        object : AnimationTimer() {
            override fun handle(now: Long) {
                loop()
            }
        }.start()

    }

    fun loop() {

        val port = selectedPort ?: return

        port.baudRate = BAUD_RATE

        val stream = port.inputStream

        buf.addAll(stream.readAllBytes().asList())



    }

    fun updatePorts() {

        ports.clear()
        SerialPort.getCommPorts().forEach {
            ports[it.descriptivePortName] = it
        }

        println("Found ports number: ${ports.size}")

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

}