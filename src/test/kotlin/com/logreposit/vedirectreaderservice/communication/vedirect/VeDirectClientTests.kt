package com.logreposit.vedirectreaderservice.communication.vedirect

import com.logreposit.vedirectreaderservice.configuration.LogrepositConfiguration
import com.logreposit.vedirectreaderservice.services.logreposit.LogrepositApiService
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.same
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.TimeUnit

class VeDirectClientTests {
    private val veDirectSerialClient: VeDirectSerialClient = mock()
    private val logrepositApiService: LogrepositApiService = mock()
    private val logrepositConfiguration: LogrepositConfiguration = mock()

    @Test
    fun `test given startListeningAndReport is called, expect VeDirectClient instance is registered as callback and VeDirectSerialClient gets triggered to start listening to serial events`() {
        val veDirectClient = veDirectClient()

        veDirectClient.startListeningAndReport()

        verify(veDirectSerialClient, times(1)).register(same(veDirectClient))
        verify(veDirectSerialClient, times(1)).startListening()
    }

    @Test
    fun `test given on VE Direct Text Protocol update, expect incoming data is mapped and pushed to logrepositApiService`() {
        val veDirectClient = veDirectClient()

        val testData = mapOf(
            "V" to "24522",
            "LOAD" to "OFF",
            "PID" to "Some Product Identifier",
            "MPPT" to "1"
        )

        veDirectClient.onVeDirectTextProtocolUpdate(testData)

        await.atMost(5, TimeUnit.SECONDS).untilAsserted {
            argumentCaptor<List<VeDirectReading<Any>>>().apply {
                verify(logrepositApiService).pushData(any(), capture())

                assertThat(allValues.size).isEqualTo(1)
                assertThat(firstValue).hasSize(4)

                assertThat(firstValue[0].field.veName).isEqualTo("V")
                assertThat(firstValue[0]).isInstanceOf(VeDirectNumberReading::class.java)
                assertThat(firstValue[0].value).isEqualTo(24522L)

                assertThat(firstValue[1].field.veName).isEqualTo("LOAD")
                assertThat(firstValue[1]).isInstanceOf(VeDirectOnOffReading::class.java)
                assertThat(firstValue[1].value).isEqualTo(false)

                assertThat(firstValue[2].field).isEqualTo(VeDirectField.PID)
                assertThat(firstValue[2]).isInstanceOf(VeDirectTextReading::class.java)
                assertThat(firstValue[2].value).isEqualTo("Some Product Identifier")

                assertThat(firstValue[3].field).isEqualTo(VeDirectField.MPPT)
                assertThat(firstValue[3]).isInstanceOf(VeDirectNumberReading::class.java)
                assertThat(firstValue[3].value).isEqualTo(1L)
            }
        }
    }

    private fun veDirectClient(): VeDirectClient {
        val threadPoolTaskExecutor = ThreadPoolTaskExecutor()

        threadPoolTaskExecutor.maxPoolSize = 1
        threadPoolTaskExecutor.initialize()

        return VeDirectClient(
            logrepositConfiguration = logrepositConfiguration,
            veDirectSerialClient = veDirectSerialClient,
            threadPoolTaskExecutor = threadPoolTaskExecutor,
            logrepositApiService = logrepositApiService
        )
    }
}
