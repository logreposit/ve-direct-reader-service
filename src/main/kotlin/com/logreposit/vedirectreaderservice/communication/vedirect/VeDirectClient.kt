package com.logreposit.vedirectreaderservice.communication.vedirect

import org.springframework.stereotype.Service
import com.logreposit.vedirectreaderservice.logger
import com.logreposit.vedirectreaderservice.services.logreposit.LogrepositApiService
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class VeDirectClient(
    private val veDirectSerialClient: VeDirectSerialClient,
    private val threadPoolTaskExecutor: ThreadPoolTaskExecutor,
    private val logrepositApiService: LogrepositApiService) : VeDirectEventListener {

    val logger = logger()

    fun startListeningAndReport() {
        veDirectSerialClient.register(this)
        veDirectSerialClient.startListening()
    }

    override fun onVeDirectTextProtocolUpdate(textData: Map<String, String>) {
        val receivedAt = Instant.now()

        threadPoolTaskExecutor.submit {
            processTextData(receivedAt, textData)
        }
    }

    private fun processTextData(receivedAt: Instant, textData: Map<String, String>) {
        logger.info("Processing VE.Direct Text Data received at $receivedAt ({} ms ago)", ChronoUnit.MILLIS.between(receivedAt, Instant.now()))

        val veDirectReadings = VeDirectMapper.map(textData)

        logrepositApiService.pushData(receivedAt, veDirectReadings)
    }
}
