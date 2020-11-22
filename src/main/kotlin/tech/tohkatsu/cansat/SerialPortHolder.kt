package tech.tohkatsu.cansat

import com.fazecast.jSerialComm.SerialPort
import java.lang.Exception

sealed class SerialPortHolder {

    abstract val port: SerialPort

}

class ActualSerialPortHolder(override val port: SerialPort) : SerialPortHolder()

object DummySerialPortHolder : SerialPortHolder() {

    override val port: SerialPort
        get() = throw Exception()

}