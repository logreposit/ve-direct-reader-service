package com.logreposit.vedirectreaderservice.services.logreposit.mappers

import com.logreposit.vedirectreaderservice.communication.vedirect.VeDirectField
import com.logreposit.vedirectreaderservice.communication.vedirect.VeDirectNumberReading
import com.logreposit.vedirectreaderservice.communication.vedirect.VeDirectOnOffReading
import com.logreposit.vedirectreaderservice.communication.vedirect.VeDirectTextReading
import com.logreposit.vedirectreaderservice.configuration.LogrepositConfiguration
import com.logreposit.vedirectreaderservice.services.logreposit.dtos.ingress.DataType
import com.logreposit.vedirectreaderservice.services.logreposit.dtos.ingress.FloatField
import com.logreposit.vedirectreaderservice.services.logreposit.dtos.ingress.IntegerField
import com.logreposit.vedirectreaderservice.services.logreposit.dtos.ingress.StringField
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

class LogrepositIngressDataMapperTests {
    @Test
    fun `test map from VeDirectReadings to IngressData`() {
        val now = Instant.now()

        val ingressData = LogrepositIngressDataMapper(LogrepositConfiguration()).toLogrepositIngressDto(
            date = now,
            address = "1",
            data = sampleVeDirectReadings()
        )

        assertThat(ingressData.readings).hasSize(1)
        assertThat(ingressData.readings[0].measurement).isEqualTo("data")
        assertThat(ingressData.readings[0].date).isEqualTo(now)
        assertThat(ingressData.readings[0].tags).hasSize(1)
        assertThat(ingressData.readings[0].tags[0].name).isEqualTo("device_address")
        assertThat(ingressData.readings[0].tags[0].value).isEqualTo("1")

        val fields = ingressData.readings[0].fields

        assertThat(fields).hasSize(8)

        assertThat(fields[0].name).isEqualTo("battery_voltage")
        assertThat(fields[0].datatype).isEqualTo(DataType.INTEGER)
        assertThat((fields[0] as IntegerField).value).isEqualTo(24528)

        assertThat(fields[1].name).isEqualTo("battery_current")
        assertThat(fields[1].datatype).isEqualTo(DataType.INTEGER)
        assertThat((fields[1] as IntegerField).value).isEqualTo(-2521L)

        assertThat(fields[2].name).isEqualTo("operation_state")
        assertThat(fields[2].datatype).isEqualTo(DataType.INTEGER)
        assertThat((fields[2] as IntegerField).value).isEqualTo(5)

        assertThat(fields[3].name).isEqualTo("operation_state_str")
        assertThat(fields[3].datatype).isEqualTo(DataType.STRING)
        assertThat((fields[3] as StringField).value).isEqualTo("FLOAT")

        assertThat(fields[4].name).isEqualTo("battery_state_of_charge")
        assertThat(fields[4].datatype).isEqualTo(DataType.INTEGER)
        assertThat((fields[4] as IntegerField).value).isEqualTo(995)

        assertThat(fields[5].name).isEqualTo("serial_number")
        assertThat(fields[5].datatype).isEqualTo(DataType.STRING)
        assertThat((fields[5] as StringField).value).isEqualTo("SomeSerialNumber")

        assertThat(fields[6].name).isEqualTo("alarm_state")
        assertThat(fields[6].datatype).isEqualTo(DataType.INTEGER)
        assertThat((fields[6] as IntegerField).value).isEqualTo(1)

        assertThat(fields[7].name).isEqualTo("relay_state")
        assertThat(fields[7].datatype).isEqualTo(DataType.INTEGER)
        assertThat((fields[7] as IntegerField).value).isEqualTo(0)
    }

    @Test
    fun `test map from VeDirectReadings to IngressData with legacy translation enabled`() {
        val now = Instant.now()

        val ingressData = LogrepositIngressDataMapper(LogrepositConfiguration().also { it.includeLegacyFields = true }).toLogrepositIngressDto(
            date = now,
            address = "1",
            data = sampleVeDirectReadings()
        )

        assertThat(ingressData.readings).hasSize(1)
        assertThat(ingressData.readings[0].measurement).isEqualTo("data")
        assertThat(ingressData.readings[0].date).isEqualTo(now)
        assertThat(ingressData.readings[0].tags).hasSize(1)
        assertThat(ingressData.readings[0].tags[0].name).isEqualTo("device_address")
        assertThat(ingressData.readings[0].tags[0].value).isEqualTo("1")

        val fields = ingressData.readings[0].fields

        assertThat(fields).hasSize(12)

        assertThat(fields[0].name).isEqualTo("battery_voltage")
        assertThat(fields[0].datatype).isEqualTo(DataType.INTEGER)
        assertThat((fields[0] as IntegerField).value).isEqualTo(24528)

        assertThat(fields[1].name).isEqualTo("battery_current")
        assertThat(fields[1].datatype).isEqualTo(DataType.INTEGER)
        assertThat((fields[1] as IntegerField).value).isEqualTo(-2521L)

        assertThat(fields[2].name).isEqualTo("current")
        assertThat(fields[2].datatype).isEqualTo(DataType.INTEGER)
        assertThat((fields[2] as IntegerField).value).isEqualTo(-2521L)

        assertThat(fields[3].name).isEqualTo("operation_state")
        assertThat(fields[3].datatype).isEqualTo(DataType.INTEGER)
        assertThat((fields[3] as IntegerField).value).isEqualTo(5)

        assertThat(fields[4].name).isEqualTo("operation_state_str")
        assertThat(fields[4].datatype).isEqualTo(DataType.STRING)
        assertThat((fields[4] as StringField).value).isEqualTo("FLOAT")

        assertThat(fields[5].name).isEqualTo("battery_state_of_charge")
        assertThat(fields[5].datatype).isEqualTo(DataType.INTEGER)
        assertThat((fields[5] as IntegerField).value).isEqualTo(995)

        assertThat(fields[6].name).isEqualTo("state_of_charge")
        assertThat(fields[6].datatype).isEqualTo(DataType.FLOAT)
        assertThat((fields[6] as FloatField).value).isEqualTo(99.5)

        assertThat(fields[7].name).isEqualTo("serial_number")
        assertThat(fields[7].datatype).isEqualTo(DataType.STRING)
        assertThat((fields[7] as StringField).value).isEqualTo("SomeSerialNumber")

        assertThat(fields[8].name).isEqualTo("alarm_state")
        assertThat(fields[8].datatype).isEqualTo(DataType.INTEGER)
        assertThat((fields[8] as IntegerField).value).isEqualTo(1)

        assertThat(fields[9].name).isEqualTo("alarm")
        assertThat(fields[9].datatype).isEqualTo(DataType.INTEGER)
        assertThat((fields[9] as IntegerField).value).isEqualTo(1)

        assertThat(fields[10].name).isEqualTo("relay_state")
        assertThat(fields[10].datatype).isEqualTo(DataType.INTEGER)
        assertThat((fields[10] as IntegerField).value).isEqualTo(0)

        assertThat(fields[11].name).isEqualTo("relay")
        assertThat(fields[11].datatype).isEqualTo(DataType.INTEGER)
        assertThat((fields[11] as IntegerField).value).isEqualTo(0)
    }

    @Test
    fun `test map from VeDirectReadings to string representation fields`() {
        val now = Instant.now()

        val ingressData = LogrepositIngressDataMapper(LogrepositConfiguration()).toLogrepositIngressDto(
            date = now,
            address = "1",
            data = listOf(
                VeDirectNumberReading(field = VeDirectField.OR, value = 2),
                VeDirectNumberReading(field = VeDirectField.CS, value = 4),
                VeDirectNumberReading(field = VeDirectField.ERR, value = 33),
                VeDirectNumberReading(field = VeDirectField.MODE, value = 253),
                VeDirectNumberReading(field = VeDirectField.MPPT, value = 1),
                VeDirectNumberReading(field = VeDirectField.MON, value = -9)
            )
        )

        assertThat(ingressData.readings).hasSize(1)
        assertThat(ingressData.readings[0].measurement).isEqualTo("data")
        assertThat(ingressData.readings[0].date).isEqualTo(now)
        assertThat(ingressData.readings[0].tags).hasSize(1)
        assertThat(ingressData.readings[0].tags[0].name).isEqualTo("device_address")
        assertThat(ingressData.readings[0].tags[0].value).isEqualTo("1")

        val fields = ingressData.readings[0].fields

        assertThat(fields).hasSize(12)

        assertThat(fields[0].name).isEqualTo("off_reason")
        assertThat(fields[0].datatype).isEqualTo(DataType.INTEGER)
        assertThat((fields[0] as IntegerField).value).isEqualTo(2L)

        assertThat(fields[1].name).isEqualTo("off_reason_str")
        assertThat(fields[1].datatype).isEqualTo(DataType.STRING)
        assertThat((fields[1] as StringField).value).isEqualTo("SWITCHED_OFF_POWER_SWITCH")

        assertThat(fields[2].name).isEqualTo("operation_state")
        assertThat(fields[2].datatype).isEqualTo(DataType.INTEGER)
        assertThat((fields[2] as IntegerField).value).isEqualTo(4L)

        assertThat(fields[3].name).isEqualTo("operation_state_str")
        assertThat(fields[3].datatype).isEqualTo(DataType.STRING)
        assertThat((fields[3] as StringField).value).isEqualTo("ABSORPTION")

        assertThat(fields[4].name).isEqualTo("error_code")
        assertThat(fields[4].datatype).isEqualTo(DataType.INTEGER)
        assertThat((fields[4] as IntegerField).value).isEqualTo(33L)

        assertThat(fields[5].name).isEqualTo("error_code_str")
        assertThat(fields[5].datatype).isEqualTo(DataType.STRING)
        assertThat((fields[5] as StringField).value).isEqualTo("INPUT_VOLTAGE_TOO_HIGH")

        assertThat(fields[6].name).isEqualTo("device_mode")
        assertThat(fields[6].datatype).isEqualTo(DataType.INTEGER)
        assertThat((fields[6] as IntegerField).value).isEqualTo(253L)

        assertThat(fields[7].name).isEqualTo("device_mode_str")
        assertThat(fields[7].datatype).isEqualTo(DataType.STRING)
        assertThat((fields[7] as StringField).value).isEqualTo("HIBERNATE")

        assertThat(fields[8].name).isEqualTo("mppt_tracker_operation_mode")
        assertThat(fields[8].datatype).isEqualTo(DataType.INTEGER)
        assertThat((fields[8] as IntegerField).value).isEqualTo(1L)

        assertThat(fields[9].name).isEqualTo("mppt_tracker_operation_mode_str")
        assertThat(fields[9].datatype).isEqualTo(DataType.STRING)
        assertThat((fields[9] as StringField).value).isEqualTo("VOLTAGE_OR_CURRENT_LIMITED")

        assertThat(fields[10].name).isEqualTo("dc_monitor_mode")
        assertThat(fields[10].datatype).isEqualTo(DataType.INTEGER)
        assertThat((fields[10] as IntegerField).value).isEqualTo(-9L)

        assertThat(fields[11].name).isEqualTo("dc_monitor_mode_str")
        assertThat(fields[11].datatype).isEqualTo(DataType.STRING)
        assertThat((fields[11] as StringField).value).isEqualTo("SOLAR_CHARGER")
    }

    private fun sampleVeDirectReadings() = listOf(
        VeDirectNumberReading(field = VeDirectField.V, value = 24528),
        VeDirectNumberReading(field = VeDirectField.I, value = -2521),
        VeDirectNumberReading(field = VeDirectField.CS, value = 5),
        VeDirectNumberReading(field = VeDirectField.SOC, value = 995),
        VeDirectTextReading(field = VeDirectField.SER, value = "SomeSerialNumber"),
        VeDirectOnOffReading(field = VeDirectField.ALARM, value = true),
        VeDirectOnOffReading(field = VeDirectField.RELAY, value = false)
    )
}
