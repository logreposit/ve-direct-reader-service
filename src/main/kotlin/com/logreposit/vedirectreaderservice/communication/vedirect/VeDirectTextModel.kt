package com.logreposit.vedirectreaderservice.communication.vedirect

sealed class VeDirectReading<out T>(val field: VeDirectField, val value: T) {
    open fun getTextRepresentation(): String? = null
}

class VeDirectNumberReading(field: VeDirectField, value: Long) : VeDirectReading<Long>(field, value) {
    override fun getTextRepresentation(): String? {
        return when (field) {
            VeDirectField.OR -> VeDirectOffReason.resolve(value).toString()
            VeDirectField.CS -> VeDirectOperationState.resolve(value).toString()
            VeDirectField.ERR -> VeDirectError.resolve(value).toString()
            VeDirectField.MODE -> VeDirectDeviceMode.resolve(value).toString()
            VeDirectField.MPPT -> VeDirectMpptTrackerOperationMode.resolve(value).toString()
            VeDirectField.MON -> VeDirectDcMonitorMode.resolve(value).toString()
            else -> null
        }
    }
}

class VeDirectOnOffReading(field: VeDirectField, value: Boolean) : VeDirectReading<Boolean>(field, value)
class VeDirectTextReading(field: VeDirectField, value: String) : VeDirectReading<String>(field, value)

enum class VeDirectValueType {
    NUMBER,
    ON_OFF,
    TEXT,
    HEX
}

enum class VeDirectField(val veName: String, val valueType: VeDirectValueType, val logrepositName: String, val logrepositDescription: String) {
    V("V", VeDirectValueType.NUMBER, "battery_voltage", ""),
    V2("V2", VeDirectValueType.NUMBER, "battery_voltage_2", ""),
    V3("V3", VeDirectValueType.NUMBER, "battery_voltage_3", ""),
    VS("VS", VeDirectValueType.NUMBER, "auxiliary_voltage", ""),
    VM("VM", VeDirectValueType.NUMBER, "mid_point_battery_voltage", ""),
    DM("DM", VeDirectValueType.NUMBER, "mid_point_battery_deviation", ""),
    VPV("VPV", VeDirectValueType.NUMBER, "panel_voltage", ""),
    PPV("PPV", VeDirectValueType.NUMBER, "panel_power", ""),
    I("I", VeDirectValueType.NUMBER, "battery_current", ""),
    I2("I2", VeDirectValueType.NUMBER, "battery_current_2", ""),
    I3("I3", VeDirectValueType.NUMBER, "battery_current_3", ""),
    IL("IL", VeDirectValueType.NUMBER, "load_current", ""),
    LOAD("LOAD", VeDirectValueType.ON_OFF, "load_output_state", ""),
    T("T", VeDirectValueType.NUMBER, "battery_temperature", ""),
    P("P", VeDirectValueType.NUMBER, "instantaneous_power", ""),
    CE("CE", VeDirectValueType.NUMBER, "consumed_energy", ""),
    SOC("SOC", VeDirectValueType.NUMBER, "state_of_charge", ""),
    TTG("TTG", VeDirectValueType.NUMBER, "time_to_go", ""),
    ALARM("Alarm", VeDirectValueType.ON_OFF, "alarm_state", ""),
    RELAY("Relay", VeDirectValueType.ON_OFF, "relay_state", ""),
    AR("AR", VeDirectValueType.NUMBER, "alarm_reason", ""),
    OR("OR", VeDirectValueType.HEX, "off_reason", ""),
    H1("H1", VeDirectValueType.NUMBER, "max_discharge_depth", ""),
    H2("H2", VeDirectValueType.NUMBER, "last_discharge_depth", ""),
    H3("H3", VeDirectValueType.NUMBER, "average_discharge_depth", ""),
    H4("H4", VeDirectValueType.NUMBER, "number_charge_cycles", ""),
    H5("H5", VeDirectValueType.NUMBER, "number_full_discharges", ""),
    H6("H6", VeDirectValueType.NUMBER, "cumulative_energy_drawn", ""),
    H7("H7", VeDirectValueType.NUMBER, "min_battery_voltage", ""),
    H8("H8", VeDirectValueType.NUMBER, "max_battery_voltage", ""),
    H9("H9", VeDirectValueType.NUMBER, "seconds_since_last_full_charge", ""),
    H10("H10", VeDirectValueType.NUMBER, "number_automatic_synchronizations", ""),
    H11("H11", VeDirectValueType.NUMBER, "number_low_battery_voltage_alarms", ""),
    H12("H12", VeDirectValueType.NUMBER, "number_high_battery_voltage_alarms", ""),
    H13("H13", VeDirectValueType.NUMBER, "number_low_auxiliary_battery_voltage_alarms", ""),
    H14("H14", VeDirectValueType.NUMBER, "number_high_auxiliary_battery_voltage_alarms", ""),
    H15("H15", VeDirectValueType.NUMBER, "min_auxiliary_battery_voltage", ""),
    H16("H16", VeDirectValueType.NUMBER, "max_auxiliary_battery_voltage", ""),
    H17("H17", VeDirectValueType.NUMBER, "discharged_energy", ""),
    H18("H18", VeDirectValueType.NUMBER, "charged_energy", ""),
    H19("H19", VeDirectValueType.NUMBER, "yield_total", ""),
    H20("H20", VeDirectValueType.NUMBER, "yield_today", ""),
    H21("H21", VeDirectValueType.NUMBER, "max_power_today", ""),
    H22("H22", VeDirectValueType.NUMBER, "yield_yesterday", ""),
    H23("H23", VeDirectValueType.NUMBER, "max_power_yesterday", ""),
    ERR("ERR", VeDirectValueType.NUMBER, "error_code", ""),
    CS("CS", VeDirectValueType.NUMBER, "operation_state", ""),
    BMV("BMV", VeDirectValueType.TEXT, "bmv_model", ""),
    FW("FW", VeDirectValueType.TEXT, "firmware_version_16", ""),
    FWE("FWE", VeDirectValueType.TEXT, "firmware_version_32", ""),
    PID("PID", VeDirectValueType.TEXT, "product_id", ""),
    SER("SER#", VeDirectValueType.TEXT, "serial_number", ""),
    HSDS("HSDS", VeDirectValueType.NUMBER, "day_sequence_number", ""),
    MODE("MODE", VeDirectValueType.NUMBER, "device_mode", ""),
    AC_OUT_V("AC_OUT_V", VeDirectValueType.NUMBER, "ac_output_voltage", ""),
    AC_OUT_I("AC_OUT_I", VeDirectValueType.NUMBER, "ac_output_current", ""),
    AC_OUT_S("AC_OUT_S", VeDirectValueType.NUMBER, "ac_output_apparent_power", ""),
    WARN("WARN", VeDirectValueType.NUMBER, "warning_reason", ""),
    MPPT("MPPT", VeDirectValueType.NUMBER, "mppt_tracker_operation_mode", ""),
    MON("MON", VeDirectValueType.NUMBER, "dc_monitor_mode", ""),
    CAP_BLE("CAP_BLE", VeDirectValueType.HEX, "bluetooth_cap", "");

    companion object {
        fun exists(veName: String) = values().any { it.veName == veName }
        fun resolve(veName: String) = values().first() { it.veName == veName }
    }
}

enum class VeDirectOffReason(val code: Long) {
    NO_INPUT_POWER(1), // 0x00000001
    SWITCHED_OFF_POWER_SWITCH(2), // 0x00000002
    SWITCHED_OFF_DEVICE_MODE_REGISTER(4), // 0x00000004
    REMOTE_INPUT(8), // 0x00000008
    PROTECTION_ACTIVE(16), // 0x00000010
    PAYGO(32), // 0x00000020
    BMS(64), // 0x00000040
    ENGINE_SHUTDOWN_DETECTION(128), // 0x00000080
    ANALYSING_INPUT_VOLTAGE(256), // 0x00000100
    UNKNOWN(Long.MIN_VALUE);

    companion object {
        fun resolve(code: Long) = values().firstOrNull { it.code == code } ?: UNKNOWN
    }
}

enum class VeDirectOperationState(val code: Long) {
    OFF(0),
    LOW_POWER(1),
    FAULT(2),
    BULK(3),
    ABSORPTION(4),
    FLOAT(5),
    STORAGE(6),
    EQUALIZE_MANUAL(7),
    INVERTING(9),
    POWER_SUPPLY(11),
    STARTING_UP(245),
    REPEATED_ABSORPTION(246),
    AUTO_EQUALIZE_OR_RECONDITION(247),
    BATTERY_SAFE(248),
    EXTERNAL_CONTROL(252),
    UNKNOWN(Long.MIN_VALUE);

    companion object {
        fun resolve(code: Long) = values().firstOrNull { it.code == code } ?: UNKNOWN
    }
}

enum class VeDirectError(val code: Long) {
    NO_ERROR(0),
    BATTERY_VOLTAGE_TOO_HIGH(2),
    CHARGER_TEMPERATURE_TOO_HIGH(17),
    CHARGER_OVER_CURRENT(18),
    CHARGER_CURRENT_REVERSED(19),
    BULK_TIME_LIMIT_EXCEEDED(20),
    CURRENT_SENSOR_ISSUE(21),
    TERMINALS_OVERHEATED(26),
    CONVERTER_ISSUE(28),
    INPUT_VOLTAGE_TOO_HIGH(33),
    INPUT_CURRENT_TOO_HIGH(34),
    INPUT_SHUTDOWN_EXCESSIVE_BATTERY_VOLTAGE(38),
    INPUT_SHUTDOWN_CURRENT_FLOW_DURING_OFF(39),
    LOST_COMMUNICATION(65),
    DEVICE_CONFIGURATION_ISSUE(66),
    BMS_CONNECTION_LOST(67),
    NETWORK_MISCONFIGURED(68),
    FACTORY_CALIBRATION_DATA_LOST(116),
    INVALID_INCOMPATIBLE_FIRMWARE(117),
    USER_SETTINGS_INVALID(119),
    UNKNOWN(Long.MIN_VALUE);

    companion object {
        fun resolve(code: Long) = values().firstOrNull { it.code == code } ?: UNKNOWN
    }
}

enum class VeDirectDeviceMode(val code: Long) {
    CHARGER(1),
    INVERTER(2),
    OFF(4),
    ECO(5),
    HIBERNATE(253),
    UNKNOWN(Long.MIN_VALUE);

    companion object {
        fun resolve(code: Long) = values().firstOrNull { it.code == code } ?: UNKNOWN
    }
}

enum class VeDirectMpptTrackerOperationMode(val code: Long) {
    OFF(0),
    VOLTAGE_OR_CURRENT_LIMITED(1),
    ACTIVE(2),
    UNKNOWN(Long.MIN_VALUE);

    companion object {
        fun resolve(code: Long) = values().firstOrNull { it.code == code } ?: UNKNOWN
    }
}

enum class VeDirectDcMonitorMode(val code: Long) {
    SOLAR_CHARGER(-9),
    WIND_TURBINE(-8),
    SHAFT_GENERATOR(-7),
    ALTERNATOR(-6),
    FUEL_CELL(-5),
    WATER_GENERATOR(-4),
    DC_DC_CHARGER(-3),
    AC_CHARGER(-2),
    GENERIC_SOURCE(-1),
    BATTERY_MONITOR_BMV(0),
    GENERIC_LOAD(1),
    ELECTRIC_DRIVE(2),
    FRIDGE(3),
    WATER_PUMP(4),
    BILGE_PUMP(5),
    DC_SYSTEM(6),
    INVERTER(7),
    WATER_HEATER(8),
    UNKNOWN(Long.MIN_VALUE);

    companion object {
        fun resolve(code: Long) = values().firstOrNull { it.code == code } ?: UNKNOWN
    }
}
