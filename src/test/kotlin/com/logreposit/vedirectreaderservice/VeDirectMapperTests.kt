package com.logreposit.vedirectreaderservice

import com.logreposit.vedirectreaderservice.communication.vedirect.VeDirectMapper
import com.logreposit.vedirectreaderservice.communication.vedirect.VeDirectTextReading
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VeDirectMapperTests {
    // MPPT:
    // PID=0xA057, FW=157, SER#=HQ2046UHRK6, V=22870, I=0, VPV=10, PPV=0, CS=0, MPPT=0, OR=0x00000001, ERR=0, LOAD=OFF, H19=0, H20=0, H21=0, H22=0, H23=0, HSDS=0

    // BMV-700
    // {PID=0x203, V=22930, I=0, P=0, CE=0, SOC=1000, TTG=-1, Alarm=OFF, Relay=OFF, AR=0, BMV=700, FW=0308}
    // {H1=-1, H2=0, H3=0, H4=0, H5=0, H6=-1, H7=6, H8=22933, H9=0, H10=0, H11=0, H12=0, H17=0, H18=0}
    //
    // after update
    //
    // {PID=0x203, V=22929, I=0, P=0, CE=0, SOC=1000, TTG=-1, Alarm=OFF, Relay=OFF, AR=0, BMV=700, FW=0311}
    // {H1=-1, H2=0, H3=0, H4=0, H5=0, H6=-1, H7=6, H8=22930, H9=0, H10=0, H11=0, H12=0, H17=0, H18=0}
    //

    @Test
    fun `test deserialized correctly`() {
        val input = mapOf(
            "PID" to "0xA057",
            "FW" to "0311",
            "SER#" to "HQ2046UHRK6",
            "V" to "22870",
            "I" to "0",
            "VPV" to "10",
            "PPV" to "0",
            "CS" to "0",
            "CE" to "0",
            "SOC" to "1000",
            "TTG" to "-1",
            "Alarm" to "OFF",
            "AR" to "0",
            "BMV" to "700",
            "Relay" to "ON",
            "MPPT" to "0",
            "OR" to "0x00000001",
            "ERR" to "0",
            "LOAD" to "OFF",
            "H1" to "-1",
            "H2" to "0",
            "H3" to "0",
            "H4" to "0",
            "H5" to "0",
            "H6" to "-1",
            "H7" to "6",
            "H8" to "22930",
            "H9" to "0",
            "H10" to "0",
            "H11" to "0",
            "H12" to "0",
            "H17" to "0",
            "H18" to "0",
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

        val valueOfFirstThing = firstThing.value

        assertThat(firstThing.value).isEqualTo(18)
    }
}
