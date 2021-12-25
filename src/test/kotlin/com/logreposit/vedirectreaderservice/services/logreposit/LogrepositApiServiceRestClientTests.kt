package com.logreposit.vedirectreaderservice.services.logreposit

import com.logreposit.vedirectreaderservice.communication.vedirect.VeDirectField
import com.logreposit.vedirectreaderservice.communication.vedirect.VeDirectNumberReading
import com.logreposit.vedirectreaderservice.communication.vedirect.VeDirectOnOffReading
import com.logreposit.vedirectreaderservice.communication.vedirect.VeDirectTextReading
import com.logreposit.vedirectreaderservice.configuration.LogrepositConfiguration
import com.logreposit.vedirectreaderservice.configuration.RetryConfiguration
import org.assertj.core.api.Assertions
import org.hamcrest.Matchers.matchesPattern
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.client.ExpectedCount
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.ResponseActions
import org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators
import org.springframework.web.client.HttpServerErrorException
import java.time.Instant

@ExtendWith(SpringExtension::class)
@RestClientTest(LogrepositApiService::class)
@Import(RetryConfiguration::class)
class LogrepositApiServiceRestClientTests {
    @MockBean
    private lateinit var logrepositConfiguration: LogrepositConfiguration

    @Autowired
    private lateinit var client: LogrepositApiService

    @Autowired
    private lateinit var server: MockRestServiceServer

    @BeforeEach
    fun setUp() {
        whenever(logrepositConfiguration.apiBaseUrl).thenReturn("https://api.logreposit.com")
    }

    @Test
    fun `given valid data it should finish successfully`() {
        server.expect(ExpectedCount.once(), requestTo("https://api.logreposit.com/v2/ingress/data"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.readings").isArray)
                .andExpect(jsonPath("$.readings.length()").value(1))
                .andExpect(jsonPath("$.readings[0].date").isString)
                .andExpect(jsonPath("$.readings[0].date").value(matchesPattern("(\\d{4})-(\\d{2})-(\\d{2})T(\\d{2})\\:(\\d{2})\\:(\\d{2})(\\.\\d+)Z")))
                .andExpect(jsonPath("$.readings[0].tags").isArray)
                .andExpect(jsonPath("$.readings[0].tags.length()").value(1))
                .andExpect(jsonPath("$.readings[0].tags[0]").isMap)
                .andExpect(jsonPath("$.readings[0].tags[0].name").value("device_address"))
                .andExpect(jsonPath("$.readings[0].tags[0].value").value("1"))
                .andExpect(jsonPath("$.readings[0].fields").isArray)
                .andExpect(jsonPath("$.readings[0].fields.length()").value(3))
                .andExpect(jsonPath("$.readings[0].fields[?(@.name == \"battery_voltage\")].value").value(24525))
                .andExpect(jsonPath("$.readings[0].fields[?(@.name == \"battery_voltage\")].datatype").value("INTEGER"))
                .andExpect(jsonPath("$.readings[0].fields[?(@.name == \"alarm_state\")].value").value(1))
                .andExpect(jsonPath("$.readings[0].fields[?(@.name == \"alarm_state\")].datatype").value("INTEGER"))
                .andExpect(jsonPath("$.readings[0].fields[?(@.name == \"product_id\")].value").value("Some Product ID"))
                .andExpect(jsonPath("$.readings[0].fields[?(@.name == \"product_id\")].datatype").value("STRING"))
                .andRespond(MockRestResponseCreators.withSuccess())

        client.pushData(receivedAt = Instant.now(), veDirectData = sampleVeDirectData())

        server.verify()
    }

    @Test
    fun `given server error when pushing data it should retry it 4 times (5 times total) before giving up`() {
        server.expect(ExpectedCount.times(5), requestTo("https://api.logreposit.com/v2/ingress/data"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withServerError())

        val started = System.currentTimeMillis()
        val now = Instant.now()

        val thrown = assertThrows<HttpServerErrorException.InternalServerError> {
            client.pushData(receivedAt = now, veDirectData = sampleVeDirectData())
        }

        Assertions.assertThat(thrown.message).isEqualTo("500 Internal Server Error: [no body]")
        Assertions.assertThat(System.currentTimeMillis() - started).isBetween(2000, 3000)

        server.verify()
    }

    @Test
    fun `given client error entity unprocessable when pushing data it update the device ingress definition`() {
        server.expect(ExpectedCount.times(1), requestTo("https://api.logreposit.com/v2/ingress/data"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.UNPROCESSABLE_ENTITY))

        val expectations = server.expect(ExpectedCount.times(1), requestTo("https://api.logreposit.com/v2/ingress/definition"))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(jsonPath("$.measurements").isArray)
                .andExpect(jsonPath("$.measurements.length()").value(1))
                .andExpect(jsonPath("$.measurements[0].name").value("data"))
                .andExpect(jsonPath("$.measurements[0].tags").isArray)
                .andExpect(jsonPath("$.measurements[0].tags.length()").value(1))
                .andExpect(jsonPath("$.measurements[0].tags[0]").value("device_address"))
                .andExpect(jsonPath("$.measurements[0].fields").isArray)
                .andExpect(jsonPath("$.measurements[0].fields.length()").value(67))

        expectIngressDefinitionUpdateBasePayload(expectations).andRespond(MockRestResponseCreators.withSuccess())

        client.pushData(receivedAt = Instant.now(), veDirectData = sampleVeDirectData())

        server.verify()
    }

    @Test
    fun `given client error entity unprocessable and legacy field publishing is enabled when pushing data it update the device ingress definition`() {
        whenever(logrepositConfiguration.includeLegacyFields).thenReturn(true)

        server.expect(ExpectedCount.times(1), requestTo("https://api.logreposit.com/v2/ingress/data"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.UNPROCESSABLE_ENTITY))

        val expectations = server.expect(ExpectedCount.times(1), requestTo("https://api.logreposit.com/v2/ingress/definition"))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(jsonPath("$.measurements").isArray)
                .andExpect(jsonPath("$.measurements.length()").value(1))
                .andExpect(jsonPath("$.measurements[0].name").value("data"))
                .andExpect(jsonPath("$.measurements[0].tags").isArray)
                .andExpect(jsonPath("$.measurements[0].tags.length()").value(1))
                .andExpect(jsonPath("$.measurements[0].tags[0]").value("device_address"))
                .andExpect(jsonPath("$.measurements[0].fields").isArray)
                .andExpect(jsonPath("$.measurements[0].fields.length()").value(67))

        // TODO DoM: adjust to also check for the legacy field definitions! Something with the mockito mocks is not correctly set up here..

        expectIngressDefinitionUpdateBasePayload(expectations).andRespond(MockRestResponseCreators.withSuccess())

        client.pushData(receivedAt = Instant.now(), veDirectData = sampleVeDirectData())

        server.verify()
    }

    private fun sampleVeDirectData() = listOf(
        VeDirectNumberReading(field = VeDirectField.V, value = 24525L),
        VeDirectOnOffReading(field = VeDirectField.ALARM, value = true),
        VeDirectTextReading(field = VeDirectField.PID, value = "Some Product ID")
    )

    private fun expectIngressDefinitionUpdateBasePayload(responseActions: ResponseActions) = responseActions.andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"battery_voltage\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"battery_voltage\")].description").value("Main or channel 1 (battery) voltage [mV]"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"battery_voltage_2\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"battery_voltage_2\")].description").value("Channel 2 (battery) voltage [mV]"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"battery_voltage_3\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"battery_voltage_3\")].description").value("Channel 3 (battery) voltage [mV]"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"auxiliary_voltage\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"auxiliary_voltage\")].description").value("Auxiliary (starter) voltage [mV]"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"mid_point_battery_voltage\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"mid_point_battery_voltage\")].description").value("Mid-point voltage of the battery bank [mV]"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"mid_point_battery_deviation\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"mid_point_battery_deviation\")].description").value("Mid-point deviation of the battery bank [‰]"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"panel_voltage\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"panel_voltage\")].description").value("Panel voltage [mV]"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"panel_power\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"panel_power\")].description").value("Panel power [W]"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"battery_current\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"battery_current\")].description").value("Main or channel 1 battery current [mA]"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"battery_current_2\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"battery_current_2\")].description").value("Channel 2 battery current [mA]"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"battery_current_3\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"battery_current_3\")].description").value("Channel 3 battery current [mA]"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"load_current\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"load_current\")].description").value("Load current [mA]"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"load_output_state\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"load_output_state\")].description").value("Load output state [ON/OFF]"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"battery_temperature\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"battery_temperature\")].description").value("Battery temperature [°C]"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"instantaneous_power\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"instantaneous_power\")].description").value("Instantaneous power [W]"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"consumed_energy\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"consumed_energy\")].description").value("Consumed Amp Hours [mAh]"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"battery_state_of_charge\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"battery_state_of_charge\")].description").value("State-of-charge [‰]"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"time_to_go\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"time_to_go\")].description").value("Time-to-go [min]"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"alarm_state\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"alarm_state\")].description").value("Alarm condition active [ON/OFF]"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"relay_state\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"relay_state\")].description").value("Relay state [ON/OFF]"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"alarm_reason\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"alarm_reason\")].description").value("Alarm reason"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"off_reason\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"off_reason\")].description").value("Off reason"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"max_discharge_depth\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"max_discharge_depth\")].description").value("Depth of the deepest discharge [mAh]"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"last_discharge_depth\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"last_discharge_depth\")].description").value("Depth of the last discharge [mAh]"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"average_discharge_depth\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"average_discharge_depth\")].description").value("Depth of the average discharge [mAh]"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"number_charge_cycles\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"number_charge_cycles\")].description").value("Number of charge cycles"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"number_full_discharges\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"number_full_discharges\")].description").value("Number of full discharges"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"cumulative_energy_drawn\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"cumulative_energy_drawn\")].description").value("Cumulative Amp Hours drawn [mAh]"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"min_battery_voltage\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"min_battery_voltage\")].description").value("Minimum main (battery) voltage [mV]"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"max_battery_voltage\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"max_battery_voltage\")].description").value("Maximum main (battery) voltage [mV]"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"seconds_since_last_full_charge\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"seconds_since_last_full_charge\")].description").value("Number of seconds since last full charge [sec]"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"number_automatic_synchronizations\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"number_automatic_synchronizations\")].description").value("Number of automatic synchronizations"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"number_low_battery_voltage_alarms\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"number_low_battery_voltage_alarms\")].description").value("Number of low main voltage alarms"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"number_high_battery_voltage_alarms\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"number_high_battery_voltage_alarms\")].description").value("Number of high main voltage alarms"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"number_low_auxiliary_battery_voltage_alarms\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"number_low_auxiliary_battery_voltage_alarms\")].description").value("Number of low auxiliary voltage alarms"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"number_high_auxiliary_battery_voltage_alarms\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"number_high_auxiliary_battery_voltage_alarms\")].description").value("Number of high auxiliary voltage alarms"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"min_auxiliary_battery_voltage\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"min_auxiliary_battery_voltage\")].description").value("Minimum auxiliary (battery) voltage [mV]"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"max_auxiliary_battery_voltage\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"max_auxiliary_battery_voltage\")].description").value("Maximum auxiliary (battery) voltage [mV]"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"discharged_energy\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"discharged_energy\")].description").value("Amount of discharged energy (BMV) / Amount of produced energy (DC monitor) [0.01 kWh]"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"charged_energy\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"charged_energy\")].description").value("Amount of charged energy (BMV) / Amount of consumed energy (DC monitor) [0.01 kWh]"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"yield_total\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"yield_total\")].description").value("Yield total (user resettable counter) [0.01 kWh]"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"yield_today\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"yield_today\")].description").value("Yield today [0.01 kWh]"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"max_power_today\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"max_power_today\")].description").value("Maximum power today [W]"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"yield_yesterday\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"yield_yesterday\")].description").value("Yield yesterday [0.01 kWh]"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"max_power_yesterday\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"max_power_yesterday\")].description").value("Maximum power yesterday [W]"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"error_code\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"error_code\")].description").value("Error code"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"operation_state\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"operation_state\")].description").value("State of operation"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"bmv_model\")].datatype").value("STRING"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"bmv_model\")].description").value("Model description (deprecated)"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"firmware_version_16\")].datatype").value("STRING"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"firmware_version_16\")].description").value("Firmware version (16 bit)"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"firmware_version_24\")].datatype").value("STRING"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"firmware_version_24\")].description").value("Firmware version (24 bit)"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"product_id\")].datatype").value("STRING"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"product_id\")].description").value("Product ID"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"serial_number\")].datatype").value("STRING"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"serial_number\")].description").value("Serial number"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"day_sequence_number\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"day_sequence_number\")].description").value("Day sequence number (0..364)"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"device_mode\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"device_mode\")].description").value("Device mode"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"ac_output_voltage\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"ac_output_voltage\")].description").value("AC output voltage [0.01 V]"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"ac_output_current\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"ac_output_current\")].description").value("AC output current [0.1 A]"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"ac_output_apparent_power\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"ac_output_apparent_power\")].description").value("AC output apparent power [VA]"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"warning_reason\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"warning_reason\")].description").value("Warning reason"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"mppt_tracker_operation_mode\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"mppt_tracker_operation_mode\")].description").value("Tracker operation mode"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"dc_monitor_mode\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"dc_monitor_mode\")].description").value("DC monitor mode"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"bluetooth_cap\")].datatype").value("INTEGER"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"bluetooth_cap\")].description").value("Bluetooth capabilities"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"off_reason_str\")].datatype").value("STRING"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"off_reason_str\")].description").value("Off reason"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"error_code_str\")].datatype").value("STRING"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"error_code_str\")].description").value("Error code"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"operation_state_str\")].datatype").value("STRING"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"operation_state_str\")].description").value("State of operation"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"device_mode_str\")].datatype").value("STRING"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"device_mode_str\")].description").value("Device mode"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"mppt_tracker_operation_mode_str\")].datatype").value("STRING"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"mppt_tracker_operation_mode_str\")].description").value("Tracker operation mode"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"dc_monitor_mode_str\")].datatype").value("STRING"))
        .andExpect(jsonPath("$.measurements[0].fields[?(@.name == \"dc_monitor_mode_str\")].description").value("DC monitor mode"))

}
