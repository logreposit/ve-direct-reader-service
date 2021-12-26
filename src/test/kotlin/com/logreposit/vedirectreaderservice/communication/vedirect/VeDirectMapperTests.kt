package com.logreposit.vedirectreaderservice.communication.vedirect

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class VeDirectMapperTests {
    @Test
    fun `test VEDirect text data of MPPT Charger is deserialized correctly`() {
        val veDirectTextData = sampleMpptVeDirectTextData()
        val veDirectReadings = VeDirectMapper.map(veDirectTextData)

        assertThat(veDirectReadings).hasSize(18)

        assertVeDirectReading(veDirectReadings[0], VeDirectField.PID, VeDirectTextReading::class.java, "0xA057")
        assertVeDirectReading(veDirectReadings[1], VeDirectField.FW, VeDirectTextReading::class.java, "157")
        assertVeDirectReading(veDirectReadings[2], VeDirectField.SER, VeDirectTextReading::class.java, "HQ2046UHRK6")
        assertVeDirectReading(veDirectReadings[3], VeDirectField.V, VeDirectNumberReading::class.java, 22870L)
        assertVeDirectReading(veDirectReadings[4], VeDirectField.I, VeDirectNumberReading::class.java, 0L)
        assertVeDirectReading(veDirectReadings[5], VeDirectField.VPV, VeDirectNumberReading::class.java, 10L)
        assertVeDirectReading(veDirectReadings[6], VeDirectField.PPV, VeDirectNumberReading::class.java, 0L)
        assertVeDirectReading(veDirectReadings[7], VeDirectField.CS, VeDirectNumberReading::class.java, 0L)
        assertVeDirectReading(veDirectReadings[8], VeDirectField.MPPT, VeDirectNumberReading::class.java, 0L)
        assertVeDirectReading(veDirectReadings[9], VeDirectField.OR, VeDirectNumberReading::class.java, 1L)
        assertVeDirectReading(veDirectReadings[10], VeDirectField.ERR, VeDirectNumberReading::class.java, 0L)
        assertVeDirectReading(veDirectReadings[11], VeDirectField.LOAD, VeDirectOnOffReading::class.java, false)
        assertVeDirectReading(veDirectReadings[12], VeDirectField.H19, VeDirectNumberReading::class.java, 0L)
        assertVeDirectReading(veDirectReadings[13], VeDirectField.H20, VeDirectNumberReading::class.java, 0L)
        assertVeDirectReading(veDirectReadings[14], VeDirectField.H21, VeDirectNumberReading::class.java, 0L)
        assertVeDirectReading(veDirectReadings[15], VeDirectField.H22, VeDirectNumberReading::class.java, 0L)
        assertVeDirectReading(veDirectReadings[16], VeDirectField.H23, VeDirectNumberReading::class.java, 0L)
        assertVeDirectReading(veDirectReadings[17], VeDirectField.HSDS, VeDirectNumberReading::class.java, 0L)
    }

    @Test
    fun `test VEDirect text data of BMV700 Battery Monitor is deserialized correctly`() {
        val veDirectTextData = sampleBmv700VeDirectTextData()
        val veDirectReadings = VeDirectMapper.map(veDirectTextData)

        assertThat(veDirectReadings).hasSize(25)

        assertVeDirectReading(veDirectReadings[0], VeDirectField.PID, VeDirectTextReading::class.java, "0x203")
        assertVeDirectReading(veDirectReadings[1], VeDirectField.V, VeDirectNumberReading::class.java, 22930L)
        assertVeDirectReading(veDirectReadings[2], VeDirectField.I, VeDirectNumberReading::class.java, 0L)
        assertVeDirectReading(veDirectReadings[3], VeDirectField.P, VeDirectNumberReading::class.java, 0L)
        assertVeDirectReading(veDirectReadings[4], VeDirectField.SOC, VeDirectNumberReading::class.java, 1000L)
        assertVeDirectReading(veDirectReadings[5], VeDirectField.TTG, VeDirectNumberReading::class.java, -1L)
        assertVeDirectReading(veDirectReadings[6], VeDirectField.ALARM, VeDirectOnOffReading::class.java, false)
        assertVeDirectReading(veDirectReadings[7], VeDirectField.RELAY, VeDirectOnOffReading::class.java, false)
        assertVeDirectReading(veDirectReadings[8], VeDirectField.AR, VeDirectNumberReading::class.java, 0L)
        assertVeDirectReading(veDirectReadings[9], VeDirectField.BMV, VeDirectTextReading::class.java, "700")
        assertVeDirectReading(veDirectReadings[10], VeDirectField.FW, VeDirectTextReading::class.java, "0308")
        assertVeDirectReading(veDirectReadings[11], VeDirectField.H1, VeDirectNumberReading::class.java, -1L)
        assertVeDirectReading(veDirectReadings[12], VeDirectField.H2, VeDirectNumberReading::class.java, 0L)
        assertVeDirectReading(veDirectReadings[13], VeDirectField.H3, VeDirectNumberReading::class.java, 0L)
        assertVeDirectReading(veDirectReadings[14], VeDirectField.H4, VeDirectNumberReading::class.java, 0L)
        assertVeDirectReading(veDirectReadings[15], VeDirectField.H5, VeDirectNumberReading::class.java, 0L)
        assertVeDirectReading(veDirectReadings[16], VeDirectField.H6, VeDirectNumberReading::class.java, -1L)
        assertVeDirectReading(veDirectReadings[17], VeDirectField.H7, VeDirectNumberReading::class.java, 6L)
        assertVeDirectReading(veDirectReadings[18], VeDirectField.H8, VeDirectNumberReading::class.java, 22933L)
        assertVeDirectReading(veDirectReadings[19], VeDirectField.H9, VeDirectNumberReading::class.java, 0L)
        assertVeDirectReading(veDirectReadings[20], VeDirectField.H10, VeDirectNumberReading::class.java, 0L)
        assertVeDirectReading(veDirectReadings[21], VeDirectField.H11, VeDirectNumberReading::class.java, 0L)
        assertVeDirectReading(veDirectReadings[22], VeDirectField.H12, VeDirectNumberReading::class.java, 0L)
        assertVeDirectReading(veDirectReadings[23], VeDirectField.H17, VeDirectNumberReading::class.java, 0L)
        assertVeDirectReading(veDirectReadings[24], VeDirectField.H18, VeDirectNumberReading::class.java, 0L)
    }

    @Test
    fun `test VEDirect text data with unknown fields, expect unknown fields are ignored`() {
        val veDirectTextData = sampleMpptVeDirectTextData() + mapOf("UNKNOWN" to "unknown value")

        val veDirectReadings = VeDirectMapper.map(veDirectTextData)

        assertThat(veDirectReadings).hasSize(18)
    }

    @Test
    fun `test parse hex value, given value does not start with 0x, expect IllegalArgumentException`() {
        assertThatThrownBy { VeDirectMapper.map(mapOf("OR" to "someInvalidValue")) }
            .isExactlyInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("HEX values must start with 0x! Got argument hex=someInvalidValue")
    }

    @Test
    fun `test parse on_off value, given invalid value, expect IllegalArgumentException`() {
        assertThatThrownBy { VeDirectMapper.map(mapOf("Alarm" to "someInvalidValue")) }
            .isExactlyInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Expected VE.Direct on/off value (case insensitive) but got veDirectValue=someInvalidValue")
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

    private fun assertVeDirectReading(veDirectReading: VeDirectReading<Any>, expectedVeDirectField: VeDirectField, expectedVeDirectInstance: Class<*>, expectedVeDirectValue: Any) {
        assertThat(veDirectReading).isNotNull
        assertThat(veDirectReading).isExactlyInstanceOf(expectedVeDirectInstance)
        assertThat(veDirectReading.field).isEqualTo(expectedVeDirectField)
        assertThat(veDirectReading.value).isEqualTo(expectedVeDirectValue)
    }
}
