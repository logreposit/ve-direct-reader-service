package com.logreposit.vedirectreaderservice.communication.vedirect

import com.logreposit.vedirectreaderservice.configuration.LogrepositConfiguration
import com.logreposit.vedirectreaderservice.logger
import com.logreposit.vedirectreaderservice.services.logreposit.LogrepositApiService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class VeDirectClient(
    private val logrepositConfiguration: LogrepositConfiguration,
    private val veDirectSerialClient: VeDirectSerialClient,
    @Qualifier("singleThreadPoolTaskExecutor") private val threadPoolTaskExecutor: ThreadPoolTaskExecutor,
    private val logrepositApiService: LogrepositApiService
) : VeDirectEventListener {

    private val lastUpdates = mutableMapOf<VeDirectField, Instant>()

    val logger = logger()

    fun startListeningAndReport() {
        veDirectSerialClient.register(this)
        veDirectSerialClient.startListening()
    }

    override fun onVeDirectTextProtocolUpdate(receivedAt: Instant, textData: Map<String, String>) {
        threadPoolTaskExecutor.submit {
            processTextData(receivedAt, textData)
        }
    }

    private fun processTextData(receivedAt: Instant, textData: Map<String, String>) {
        logger.info("Processing VE.Direct Text Data received at $receivedAt ({} ms ago)", ChronoUnit.MILLIS.between(receivedAt, Instant.now()))

        VeDirectMapper.map(textData).also {
            push(receivedAt, it)
        }
    }

    @Synchronized
    private fun push(receivedAt: Instant, veDirectReadings: List<VeDirectReading<Any>>) {
        val updateInterval = logrepositConfiguration.minimumUpdateIntervalInMillis ?: 10000

        if (shouldPush(receivedAt, veDirectReadings, updateInterval)) {
            logrepositApiService.pushData(receivedAt, veDirectReadings)

            updateLastUpdated(receivedAt, veDirectReadings)
        }
    }

    private fun shouldPush(receivedAt: Instant, veDirectReadings: List<VeDirectReading<Any>>, updateInterval: Long): Boolean {
        if (updateInterval == 0L) {
            return true
        }

        veDirectReadings.forEach {
            val lastUpdatedAt = lastUpdates[it.field] ?: return true

            val offset = Duration.between(lastUpdatedAt, receivedAt)
            val millis = offset.toMillis() // TODO check positive/negative

            if (millis >= updateInterval) {
                return true
            }
        }

        return false
    }

    private fun updateLastUpdated(receivedAt: Instant, veDirectReadings: List<VeDirectReading<Any>>) = veDirectReadings.forEach {
        lastUpdates[it.field] = receivedAt
    }
}
