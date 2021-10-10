package com.logreposit.vedirectreaderservice

import org.junit.jupiter.api.Test
import kotlin.concurrent.thread

class SerialClientTests {

    class MyListener : VeDirectEventListener {
        override fun onVeDirectTextProtocolUpdate(textData: Map<String, String>) {
            println("Listening to Event: $textData")
        }
    }

    @Test
    fun `test read from serial line per line`() {
        val serialClient = VeDirectSerialClient(device = "/dev/tty.usbserial-VE2OV1G5").also {
            it.register(MyListener())
        }

        serialClient.open()

        thread {
            serialClient.readData()
        }

        //Thread.sleep(15000)

        var i = 0;

        val command = ":451\n"
        val commandBytes = stringToASCIIBytes(command)

        while (true) {
            i++

            Thread.sleep(1000)

            if (i == 8) {
                print("ACTION! ==> ")
                val bytesSent = serialClient.sendCommand(commandBytes)
                println("Sent bytes: $bytesSent")

                i = 0
            }
        }
    }

    private fun stringToASCIIBytes(string: String): ByteArray {
        val b = ByteArray(string.length)

        for (i in b.indices) {
            b[i] = string[i].code.toByte()
        }

        return b
    }

}
