package com.logreposit.vedirectreaderservice.communication.vedirect

import com.fazecast.jSerialComm.SerialPort
import com.logreposit.vedirectreaderservice.configuration.VeDirectConfiguration
import com.logreposit.vedirectreaderservice.logger
import org.springframework.stereotype.Service

interface VeDirectEventListener {
    fun onVeDirectTextProtocolUpdate(textData: Map<String, String>)
}

@OptIn(ExperimentalUnsignedTypes::class)
@Service
class VeDirectSerialClient(veDirectConfiguration: VeDirectConfiguration) {

    private enum class ReaderState {
        BEGIN, LABEL, VALUE, CHECKSUM, HEX
    }

    companion object {
        const val CR = 0x0D.toByte()
        const val LF = 0x0A.toByte()
        const val HEX = 0x3A.toByte()
        const val TAB = 0x09.toByte()
    }

    private val logger = logger()

    private val eventListeners = mutableListOf<VeDirectEventListener>()

    private val inputBuffer = mutableListOf<Byte>()
    private val labelBuffer = mutableListOf<Byte>()
    private val valueBuffer = mutableListOf<Byte>()
    private val hexBuffer = mutableListOf<Byte>()

    private val resultMap = mutableMapOf<String, String>()

    private val serialPort: SerialPort = SerialPort.getCommPort(veDirectConfiguration.comPort)

    private var state: ReaderState = ReaderState.BEGIN

    fun register(veDirectEventListener: VeDirectEventListener) = eventListeners.add(veDirectEventListener)

    fun startListening() {
        closeSerialPort()
        openSerialPort()

        while (true) {
            val bytesAvailable = serialPort.bytesAvailable()

            if (bytesAvailable < 1) {
                continue
            }

            val readBuffer = ByteArray(bytesAvailable)

            serialPort.readBytes(readBuffer, readBuffer.size.toLong())

            for (b in readBuffer) {
                processByte(b)
            }
        }
    }

    private fun closeSerialPort() {
        if (serialPort.isOpen) {
            serialPort.closePort()
        }
    }

    private fun openSerialPort() {
        configureSerialPort()
        serialPort.openPort()
    }

    fun sendCommand(byteArray: ByteArray): Int {
        return serialPort.writeBytes(byteArray, byteArray.size.toLong())
    }

    private fun configureSerialPort() {
        serialPort.baudRate = 19200
        serialPort.numDataBits = 8
        serialPort.numStopBits = 1
        serialPort.parity = SerialPort.NO_PARITY
        serialPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED)
    }

    private fun processByte(byte: Byte) {
        if (byte == HEX && state != ReaderState.CHECKSUM) {
            hexBuffer.clear()
            state = ReaderState.HEX
            logger.debug("HEX ==> RECEIVED HEX MSG START")
            return
        }

        inputBuffer.add(byte)

        when (state) {
            ReaderState.HEX -> inHex(byte)
            ReaderState.BEGIN -> inWaitBegin(byte)
            ReaderState.LABEL -> inLabel(byte)
            ReaderState.VALUE -> inValue(byte)
            ReaderState.CHECKSUM -> inChecksum()
        }
    }

    private fun inHex(byte: Byte) {
        logger.debug("HEX ==> IN HEX MODE!")
        //inputBuffer.clear()

        if (byte != LF) {
            hexBuffer.add(byte)

            return;
        }

        inputBuffer.clear()

        logger.debug("HEX Response is complete (LF), content: ${String(hexBuffer.toByteArray())}")

        val parsedCommand = hexMsgToByteArray()

        logger.debug("parsedCommand: $parsedCommand")

        val validChecksum = isValidChecksum(parsedCommand)

        if (validChecksum) {
            logger.debug("VALID HEX CHECKSUM :D")
        } else {
            logger.debug("INVALID HEX CHECKSUM!! :'(")
        }

        state = ReaderState.BEGIN
    }

    private fun hexMsgToByteArray(): UByteArray {
        // https://www.rapidtables.com/convert/number/hex-to-decimal.html
        // e.g. A0002000148
        // should become 0xA 0x00 0X02 0X00 0x01 0x48 -> 6 items
        val hexMsg = String(hexBuffer.toByteArray())
        val command = hexMsg.take(1)
        val hexMsgWithoutCommand = hexMsg.drop(1)
        val parsedCommand = mutableListOf<UByte>()

        parsedCommand.add(command.toInt(16).toUByte())

        hexMsgWithoutCommand.chunkedSequence(2)
            .map { it.toInt(16).toUByte() }
            .toList()
            .forEach {
                parsedCommand.add(it)
            }

        val byteArray = parsedCommand.toUByteArray()

        return byteArray
    }

    private fun isValidChecksum(byteArray: UByteArray): Boolean {
        var checksum = 0x55.toUByte()

        for (b in byteArray) {
            checksum = (checksum - b).toUByte()
        }

        return (checksum == 0.toUByte());
    }

    private fun inWaitBegin(byte: Byte) {
        if (byte == LF) {
            state = ReaderState.LABEL
        } else {
            resultMap.clear()
        }
    }

    private fun inLabel(byte: Byte) {
        if (byte != TAB) {
            labelBuffer.add(byte)

            return
        }

        val label = String(labelBuffer.toByteArray())

        state = when (label) {
            "Checksum" -> ReaderState.CHECKSUM
            else -> ReaderState.VALUE
        }
    }

    private fun inValue(byte: Byte) {
        if (byte != CR) {
            valueBuffer.add(byte)

            return
        }

        state = ReaderState.BEGIN

        val label = String(labelBuffer.toByteArray())
        val value = String(valueBuffer.toByteArray())

        resultMap[label] = value

        labelBuffer.clear()
        valueBuffer.clear()
    }

    private fun inChecksum() {
        labelBuffer.clear()
        valueBuffer.clear()

        state = ReaderState.BEGIN

        val allRead = inputBuffer.sum()

        if (allRead % 256 == 0) {
            eventListeners.forEach { it.onVeDirectTextProtocolUpdate(resultMap.toMap()) }
        } else {
            logger.warn("Checksum of VE.Direct block read is invalid.")
        }

        inputBuffer.clear()
    }
}
