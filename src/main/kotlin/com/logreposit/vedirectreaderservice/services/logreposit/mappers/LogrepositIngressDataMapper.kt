package com.logreposit.vedirectreaderservice.services.logreposit.mappers

import com.logreposit.vedirectreaderservice.communication.vedirect.VeDirectField
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
import com.logreposit.vedirectreaderservice.services.logreposit.dtos.ingress.FloatField
import java.time.Instant

class LogrepositIngressDataMapper(private val includeLegacyFields: Boolean) {
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
        val legacyField = getLegacyReading(reading)

        return listOfNotNull(field, textRepresentationField, legacyField)
    }

    private fun boolToLong(bool: Boolean) = when (bool) {
        true -> 1L
        false -> 0L
    }

    // To be backwards compatible to the old bmv-reader-service (BMV-600S)
    private fun getLegacyReading(reading: VeDirectReading<Any>): Field? {
        if (!includeLegacyFields) {
            return null
        }

        return when (reading.field) {
            VeDirectField.ALARM -> if (reading is VeDirectOnOffReading) IntegerField(name = "alarm", value = boolToLong(reading.value)) else null
            VeDirectField.RELAY -> if (reading is VeDirectOnOffReading) IntegerField(name = "relay", value = boolToLong(reading.value)) else null
            VeDirectField.I -> if (reading is VeDirectNumberReading) IntegerField(name = "current", value = reading.value) else null
            VeDirectField.SOC -> if (reading is VeDirectNumberReading) FloatField(name = "state_of_charge", value = reading.value * 0.1) else null
            else -> null
        }
    }
}
