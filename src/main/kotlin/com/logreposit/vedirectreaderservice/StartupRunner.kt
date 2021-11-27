package com.logreposit.vedirectreaderservice

import com.fazecast.jSerialComm.SerialPort
import com.logreposit.vedirectreaderservice.communication.vedirect.VeDirectClient
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class StartupRunner(private val veDirectClient: VeDirectClient) : CommandLineRunner {
    val logger = logger()

    override fun run(vararg args: String?) {
        logger.info("Startup-Runner: Starting to listen to VE.Direct frames")

        logger.info("Available com ports: {}", SerialPort.getCommPorts())

        veDirectClient.startListeningAndReport()
    }
}
