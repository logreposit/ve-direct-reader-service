package com.logreposit.vedirectreaderservice.communication.vedirect

import com.logreposit.vedirectreaderservice.logger
import java.util.Locale

object VeDirectMapper {
    private val log = logger()

    fun map(veDirectTextData: Map<String, String>): List<VeDirectReading<Any>> {
        logUnknownFields(veDirectTextData)

        return mapTextData(veDirectTextData)
    }

    private fun logUnknownFields(veDirectTextData: Map<String, String>) = veDirectTextData
        .filterNot { VeDirectField.exists(it.key) }
        .also {
            if (it.isNotEmpty()) {
                log.info("The following VE.Direct fields got from the device are not (yet) implemented: $it.")
            }
        }

    private fun mapTextData(veDirectTextData: Map<String, String>) = veDirectTextData
        .filter { VeDirectField.exists(it.key) }
        .filter { it.value != "---" }
        .map { VeDirectField.resolve(it.key) to it.value }
        .map {
            when (it.first.valueType) {
                VeDirectValueType.NUMBER -> VeDirectNumberReading(it.first, value = it.second.toLong())
                VeDirectValueType.ON_OFF -> VeDirectOnOffReading(it.first, value = parseBooleanFromOnOff(it.second))
                VeDirectValueType.TEXT -> VeDirectTextReading(it.first, it.second)
                VeDirectValueType.HEX -> VeDirectNumberReading(it.first, parseNumberFromHexString(it.second))
            }
        }

    private fun parseBooleanFromOnOff(veDirectValue: String): Boolean {
        return when (veDirectValue.lowercase(Locale.US)) {
            "on" -> true
            "off" -> false
            else -> throw java.lang.IllegalArgumentException("Expected VE.Direct on/off value (case insensitive) but got veDirectValue=$veDirectValue")
        }
    }

    private fun parseNumberFromHexString(hex: String): Long {
        if (!hex.lowercase().startsWith("0x")) {
            throw IllegalArgumentException("HEX values must start with 0x! Got argument hex=$hex")
        }

        return hex.removePrefix("0x").toLong(radix = 16)
    }
}
