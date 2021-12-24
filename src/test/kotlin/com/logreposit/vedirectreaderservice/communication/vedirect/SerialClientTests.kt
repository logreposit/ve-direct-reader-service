package com.logreposit.vedirectreaderservice.communication.vedirect

import com.logreposit.vedirectreaderservice.configuration.VeDirectConfiguration
import com.logreposit.vedirectreaderservice.logger
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread

class SerialClientTests {
    class MyListener : VeDirectEventListener {
        private val log = logger()

        override fun onVeDirectTextProtocolUpdate(textData: Map<String, String>) {
            val dateTime = DateTimeFormatter.ISO_INSTANT.format(Instant.now());

            log.info("$dateTime => Received VE.Direct Text Protocol Event: $textData")
        }
    }

    private val log = logger()

    @Test
    fun `test smth`() {
        var myVeField = VeDirectField.resolve("V")

        assertThat(myVeField).isEqualTo(VeDirectField.V)

        var mySecondVeField = VeDirectField.resolve("BLAH")

        assertThat(mySecondVeField).isEqualTo(VeDirectField.V)
    }

    @Test
    fun `test read from serial line per line`() {
        val config = VeDirectConfiguration()

        config.comPort = "/dev/tty.usbserial-VE2OV1G5"

        val serialClient = VeDirectSerialClient(config).also {
            it.register(MyListener())
        }

        thread {
            serialClient.startListening()
        }

        while (true) { }
    }

    @Test
    fun `test read from serial line per line with additional command sending`() {
        val config = VeDirectConfiguration()

        config.comPort = "/dev/tty.usbserial-VE2OV1G5"

        val serialClient = VeDirectSerialClient(config).also {
            it.register(MyListener())
        }

        thread {
            serialClient.startListening()
        }

        Thread.sleep(15000)

        var i = 0;

        val command = ":451\n"
        val commandBytes = stringToASCIIBytes(command)

        while (true) {
            i++

            Thread.sleep(1000)

            if (i == 8) {
                log.debug("ACTION! ==> ")
                val bytesSent = serialClient.sendCommand(commandBytes)
                log.debug("Sent bytes: $bytesSent")

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
