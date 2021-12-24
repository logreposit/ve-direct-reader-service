package com.logreposit.vedirectreaderservice.communication.vedirect

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VeDirectMapperTests {
    @Test
    fun `test VEDirect text data of MPPT Charger is deserialized correctly`() {
        val veDirectTextData = sampleMpptVeDirectTextData()
        val veDirectReadings = VeDirectMapper.map(veDirectTextData)

        assertThat(veDirectReadings).hasSize(18)

        val firstThing = veDirectReadings[0]

        assertThat(firstThing).isExactlyInstanceOf(VeDirectTextReading::class.java)
        assertThat(firstThing.value).isEqualTo("0xA057")
    }

    @Test
    fun `test VEDirect text data of BMV700 Battery Monitor is deserialized correctly`() {
        val veDirectTextData = sampleBmv700VeDirectTextData()
        val veDirectReadings = VeDirectMapper.map(veDirectTextData)

        assertThat(veDirectReadings).hasSize(25)
    }

    @Test
    fun `test VEDirect text data with unknown fields, expect unknown fields are ignored`() {
        val veDirectTextData = sampleMpptVeDirectTextData() + mapOf("UNKNOWN" to "unknown value")

        val veDirectReadings = VeDirectMapper.map(veDirectTextData)

        assertThat(veDirectReadings).hasSize(18)
    }

    private fun sampleMpptVeDirectTextData() = mapOf(
        "PID" to "0xA057",
        "FW" to "157",
        "SER#" to "HQ2046UHRK6",
        "V" to "22870",
        "I" to "0",
        "VPV" to "10",
        "PPV" to "0",
        "CS" to "0",
        "MPPT" to "0",
        "OR" to "0x00000001",
        "ERR" to "0",
        "LOAD" to "OFF",
        "H19" to "0",
        "H20" to "0",
        "H21" to "0",
        "H22" to "0",
        "H23" to "0",
        "HSDS" to "0"
    )

    private fun sampleBmv700VeDirectTextData() = mapOf(
        "PID" to "0x203",
        "V" to "22930",
        "I" to "0",
        "P" to "0",
        "SOC" to "1000",
        "TTG" to "-1",
        "Alarm" to "OFF",
        "Relay" to "OFF",
        "AR" to "0",
        "BMV" to "700",
        "FW" to "0308",
        "H1" to "-1",
        "H2" to "0",
        "H3" to "0",
        "H4" to "0",
        "H5" to "0",
        "H6" to "-1",
        "H7" to "6",
        "H8" to "22933",
        "H9" to "0",
        "H10" to "0",
        "H11" to "0",
        "H12" to "0",
        "H17" to "0",
        "H18" to "0"
    )
}
