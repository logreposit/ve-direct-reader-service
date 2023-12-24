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
import org.mockito.kotlin.whenever
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.time.Instant
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

        veDirectClient.onVeDirectTextProtocolUpdate(Instant.now(), testData)

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

    @Test
    fun `test given on multiple VE Direct Text Protocol updates within the min update interval setting, expect incoming data is mapped and pushed to logrepositApiService only once`() {
        whenever(logrepositConfiguration.minimumUpdateIntervalInMillis).thenReturn(5000L)

        val veDirectClient = veDirectClient()

        val testData = mapOf(
            "V" to "24522",
            "LOAD" to "OFF",
            "PID" to "Some Product Identifier",
            "MPPT" to "1"
        )

        val now = Instant.now()

        val update1 = now.minusMillis(3000)
        val update2 = now.minusMillis(2000)
        val update3 = now.minusMillis(1000)
        val update4 = now

        veDirectClient.onVeDirectTextProtocolUpdate(update1, testData)
        veDirectClient.onVeDirectTextProtocolUpdate(update2, testData)
        veDirectClient.onVeDirectTextProtocolUpdate(update3, testData)
        veDirectClient.onVeDirectTextProtocolUpdate(update4, testData)

        assertThatLogrepositApiServiceIsCalled(times = 1)
    }

    @Test
    fun `test given on multiple VE Direct Text Protocol updates not all within the min update interval setting, expect incoming data is mapped and two updates are pushed to logrepositApiService`() {
        whenever(logrepositConfiguration.minimumUpdateIntervalInMillis).thenReturn(5000L)

        val veDirectClient = veDirectClient()

        val testData = mapOf(
            "V" to "24522",
            "LOAD" to "OFF",
            "PID" to "Some Product Identifier",
            "MPPT" to "1"
        )

        val now = Instant.now()

        val update1 = now.minusMillis(5100)
        val update2 = now.minusMillis(2000)
        val update3 = now.minusMillis(1000)
        val update4 = now

        veDirectClient.onVeDirectTextProtocolUpdate(update1, testData)
        veDirectClient.onVeDirectTextProtocolUpdate(update2, testData)
        veDirectClient.onVeDirectTextProtocolUpdate(update3, testData)
        veDirectClient.onVeDirectTextProtocolUpdate(update4, testData)

        assertThatLogrepositApiServiceIsCalled(times = 2)
    }

    @Test
    fun `test given on multiple VE Direct Text Protocol updates and all within the min update interval setting, subsequent updates contain new fields, expect incoming data is mapped and all updates are pushed to logrepositApiService`() {
        // Use case: SoC becomes available a bit later, but once it has become available we want to push the complete series again.

        whenever(logrepositConfiguration.minimumUpdateIntervalInMillis).thenReturn(5000L)

        val veDirectClient = veDirectClient()

        val testData1 = mapOf(
            "V" to "24522",
            "LOAD" to "OFF",
            "PID" to "Some Product Identifier",
            "MPPT" to "1"
        )

        val testData2 = testData1 + mapOf("SOC" to "9952")
        val testData3 = testData1 + mapOf("OR" to "0x00000010")
        val testData4 = testData1 + testData2 + mapOf("V" to "24000")

        val now = Instant.now()

        val update1 = now.minusMillis(3000)
        val update2 = now.minusMillis(2000)
        val update3 = now.minusMillis(1000)
        val update4 = now

        veDirectClient.onVeDirectTextProtocolUpdate(update1, testData1)
        veDirectClient.onVeDirectTextProtocolUpdate(update2, testData2)
        veDirectClient.onVeDirectTextProtocolUpdate(update3, testData3)
        veDirectClient.onVeDirectTextProtocolUpdate(update4, testData4)

        assertThatLogrepositApiServiceIsCalled(times = 3)
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

    private fun assertThatLogrepositApiServiceIsCalled(times: Int) = await
        .atMost(10, TimeUnit.SECONDS)
        .pollDelay(5, TimeUnit.SECONDS)
        .untilAsserted {
            argumentCaptor<List<VeDirectReading<Any>>>().apply {
                verify(logrepositApiService, times(times)).pushData(any(), capture())

                assertThat(allValues.size).isEqualTo(times)
            }
        }
}
