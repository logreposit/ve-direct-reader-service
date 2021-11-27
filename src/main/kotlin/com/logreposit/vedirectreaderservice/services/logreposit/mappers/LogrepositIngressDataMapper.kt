package com.logreposit.vedirectreaderservice.services.logreposit.mappers

import com.logreposit.vedirectreaderservice.communication.vedirect.VeDirectNumberReading
import com.logreposit.vedirectreaderservice.communication.vedirect.VeDirectOnOffReading
import com.logreposit.vedirectreaderservice.services.logreposit.dtos.ingress.IngressData
import com.logreposit.vedirectreaderservice.services.logreposit.dtos.ingress.IntegerField
import com.logreposit.vedirectreaderservice.services.logreposit.dtos.ingress.Reading
import com.logreposit.vedirectreaderservice.services.logreposit.dtos.ingress.StringField
import com.logreposit.vedirectreaderservice.services.logreposit.dtos.ingress.Tag
import com.logreposit.vedirectreaderservice.communication.vedirect.VeDirectReading
import com.logreposit.vedirectreaderservice.communication.vedirect.VeDirectTextReading
import com.logreposit.vedirectreaderservice.services.logreposit.dtos.ingress.Field
import java.time.Instant

object LogrepositIngressDataMapper {
    fun toLogrepositIngressDto(date: Instant, address: String, data: List<VeDirectReading<Any>>) = IngressData(
            readings = listOf(
                    Reading(
                            date = date,
                            measurement = "data",
                            tags = listOf(Tag(name = "device_address", value = address)),
                            fields = data.flatMap { transformVeDirectReading(it) }
                    )
            )
    )

    private fun transformVeDirectReading(reading: VeDirectReading<Any>): List<Field> {
        val name = reading.field.logrepositName

        val field = when (reading) {
            is VeDirectNumberReading -> IntegerField(name = name, value = reading.value)
            is VeDirectOnOffReading -> IntegerField(name = name, value = boolToLong(reading.value))
            is VeDirectTextReading -> StringField(name = name, value = reading.value)
        }

        val textRepresentationField = reading.getTextRepresentation()?.let { StringField(name = "${name}_str", value = it)}

        return listOfNotNull(field, textRepresentationField)
    }

    private fun boolToLong(bool: Boolean) = when (bool) {
        true -> 1L
        false -> 0L
    }

    private fun createFault(date: Instant, address: String, faultName: String, state: Boolean) = Reading(
            date = date,
            measurement = "faults",
            tags = listOfNotNull(
                    Tag(name = "device_address", value = address),
                    Tag(name = "fault_name", value = faultName)
            ),
            fields = listOfNotNull(
                    IntegerField(name = "state", value = boolToLong(state)),
                    StringField(name = "state_str", value = stateToString(state = state))
            )
    )

    private fun stateToString(state: Boolean) = when (state) {
        true -> "ERROR"
        false -> "OK"
    }
}
