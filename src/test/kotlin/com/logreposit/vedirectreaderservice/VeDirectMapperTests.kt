package com.logreposit.vedirectreaderservice

import com.logreposit.vedirectreaderservice.communication.vedirect.VeDirectMapper
import com.logreposit.vedirectreaderservice.communication.vedirect.VeDirectTextReading
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VeDirectMapperTests {
    // PID=0xA057, FW=157, SER#=HQ2046UHRK6, V=22870, I=0, VPV=10, PPV=0, CS=0, MPPT=0, OR=0x00000001, ERR=0, LOAD=OFF, H19=0, H20=0, H21=0, H22=0, H23=0, HSDS=0

    @Test
    fun `test deserialized correctly`() {
        val input = mapOf(
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

        val deserialized = VeDirectMapper.map(input)

        assertThat(deserialized).hasSize(18)

        val firstThing = deserialized[0]

        assertThat(firstThing).isExactlyInstanceOf(VeDirectTextReading::class.java)

        val valueOfFirstThing = firstThing.getValue()

        assertThat(firstThing.getValue()).isEqualTo(18)
    }
}
