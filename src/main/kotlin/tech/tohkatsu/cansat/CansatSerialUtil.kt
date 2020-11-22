package tech.tohkatsu.cansat

import com.fazecast.jSerialComm.SerialPort
import javafx.animation.AnimationTimer
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.stage.Stage

class CansatSerialUtil : Application() {

    companion object {
        const val TITLE = "Cansat Serial Utility"
    }

    lateinit var stage: Stage
        private set

    lateinit var controller: MainController
        private set

    override fun start(stage: Stage) {

        this.stage = stage

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



    }

    fun updatePorts() {

        val ports = SerialPort.getCommPorts()

        controller.choicebox_port.items.clear()

        if(ports.size > 1) {

            controller.choicebox_port.isDisable = false

            controller.choicebox_port.items.addAll(ports.map { ActualSerialPortHolder(it) })

        } else {

            controller.choicebox_port.isDisable = true

            controller.choicebox_port.items.add(DummySerialPortHolder) // be careful!

        }

    }

}