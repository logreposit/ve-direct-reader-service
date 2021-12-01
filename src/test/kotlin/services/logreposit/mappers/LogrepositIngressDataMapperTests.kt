package services.logreposit.mappers

import com.logreposit.vedirectreaderservice.communication.vedirect.VeDirectField
import com.logreposit.vedirectreaderservice.communication.vedirect.VeDirectNumberReading
import com.logreposit.vedirectreaderservice.communication.vedirect.VeDirectOnOffReading
import com.logreposit.vedirectreaderservice.communication.vedirect.VeDirectTextReading
import com.logreposit.vedirectreaderservice.services.logreposit.dtos.ingress.DataType
import com.logreposit.vedirectreaderservice.services.logreposit.dtos.ingress.IntegerField
import com.logreposit.vedirectreaderservice.services.logreposit.dtos.ingress.StringField
import com.logreposit.vedirectreaderservice.services.logreposit.mappers.LogrepositIngressDataMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

class LogrepositIngressDataMapperTests {
    @Test
    fun `test map from VeDirectReadings to IngressData`() {
        val now = Instant.now()

        val ingressData = LogrepositIngressDataMapper.toLogrepositIngressDto(
            date = now,
            address = "1",
            data = listOf(
                VeDirectNumberReading(field = VeDirectField.V, value = 24528),
                VeDirectNumberReading(field = VeDirectField.CS, value = 5),
                VeDirectTextReading(field = VeDirectField.SER, value = "SomeSerialNumber"),
                VeDirectOnOffReading(field = VeDirectField.ALARM, value = true)
            )
        )

        assertThat(ingressData.readings).hasSize(1)
        assertThat(ingressData.readings[0].measurement).isEqualTo("data")
        assertThat(ingressData.readings[0].date).isEqualTo(now)
        assertThat(ingressData.readings[0].tags).hasSize(1)
        assertThat(ingressData.readings[0].tags[0].name).isEqualTo("device_address")
        assertThat(ingressData.readings[0].tags[0].value).isEqualTo("1")

        val fields = ingressData.readings[0].fields

        assertThat(fields).hasSize(5)

        assertThat(fields[0].name).isEqualTo("battery_voltage")
        assertThat(fields[0].datatype).isEqualTo(DataType.INTEGER)
        assertThat((fields[0] as IntegerField).value).isEqualTo(24528)

        assertThat(fields[1].name).isEqualTo("operation_state")
        assertThat(fields[1].datatype).isEqualTo(DataType.INTEGER)
        assertThat((fields[1] as IntegerField).value).isEqualTo(5)

        assertThat(fields[2].name).isEqualTo("operation_state_str")
        assertThat(fields[2].datatype).isEqualTo(DataType.STRING)
        assertThat((fields[2] as StringField).value).isEqualTo("FLOAT")

        assertThat(fields[3].name).isEqualTo("serial_number")
        assertThat(fields[3].datatype).isEqualTo(DataType.STRING)
        assertThat((fields[3] as StringField).value).isEqualTo("SomeSerialNumber")

        assertThat(fields[4].name).isEqualTo("alarm_state")
        assertThat(fields[4].datatype).isEqualTo(DataType.INTEGER)
        assertThat((fields[4] as IntegerField).value).isEqualTo(1)
    }
}
