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

enum class VeDirectField(val veName: String, val valueType: VeDirectValueType, val logrepositName: String, val logrepositDescription: String, val hasStringRepresentation: Boolean) {
    V("V", VeDirectValueType.NUMBER, "battery_voltage", "Main or channel 1 (battery) voltage [mV]", false),
    V2("V2", VeDirectValueType.NUMBER, "battery_voltage_2", "Channel 2 (battery) voltage [mV]", false),
    V3("V3", VeDirectValueType.NUMBER, "battery_voltage_3", "Channel 3 (battery) voltage [mV]", false),
    VS("VS", VeDirectValueType.NUMBER, "auxiliary_voltage", "Auxiliary (starter) voltage [mV]", false),
    VM("VM", VeDirectValueType.NUMBER, "mid_point_battery_voltage", "Mid-point voltage of the battery bank [mV]", false),
    DM("DM", VeDirectValueType.NUMBER, "mid_point_battery_deviation", "Mid-point deviation of the battery bank [‰]", false),
    VPV("VPV", VeDirectValueType.NUMBER, "panel_voltage", "Panel voltage [mV]", false),
    PPV("PPV", VeDirectValueType.NUMBER, "panel_power", "Panel power [W]", false),
    I("I", VeDirectValueType.NUMBER, "battery_current", "Main or channel 1 battery current [mA]", false),
    I2("I2", VeDirectValueType.NUMBER, "battery_current_2", "Channel 2 battery current [mA]", false),
    I3("I3", VeDirectValueType.NUMBER, "battery_current_3", "Channel 3 battery current [mA]", false),
    IL("IL", VeDirectValueType.NUMBER, "load_current", "Load current [mA]", false),
    LOAD("LOAD", VeDirectValueType.ON_OFF, "load_output_state", "Load output state [ON/OFF]", false),
    T("T", VeDirectValueType.NUMBER, "battery_temperature", "Battery temperature [°C]", false),
    P("P", VeDirectValueType.NUMBER, "instantaneous_power", "Instantaneous power [W]", false),
    CE("CE", VeDirectValueType.NUMBER, "consumed_energy", "Consumed Amp Hours [mAh]", false),
    SOC("SOC", VeDirectValueType.NUMBER, "battery_state_of_charge", "State-of-charge [‰]", false),
    TTG("TTG", VeDirectValueType.NUMBER, "time_to_go", "Time-to-go [min]", false),
    ALARM("Alarm", VeDirectValueType.ON_OFF, "alarm_state", "Alarm condition active [ON/OFF]", false),
    RELAY("Relay", VeDirectValueType.ON_OFF, "relay_state", "Relay state [ON/OFF]", false),
    AR("AR", VeDirectValueType.NUMBER, "alarm_reason", "Alarm reason", false),
    OR("OR", VeDirectValueType.HEX, "off_reason", "Off reason", true),
    H1("H1", VeDirectValueType.NUMBER, "max_discharge_depth", "Depth of the deepest discharge [mAh]", false),
    H2("H2", VeDirectValueType.NUMBER, "last_discharge_depth", "Depth of the last discharge [mAh]", false),
    H3("H3", VeDirectValueType.NUMBER, "average_discharge_depth", "Depth of the average discharge [mAh]", false),
    H4("H4", VeDirectValueType.NUMBER, "number_charge_cycles", "Number of charge cycles", false),
    H5("H5", VeDirectValueType.NUMBER, "number_full_discharges", "Number of full discharges", false),
    H6("H6", VeDirectValueType.NUMBER, "cumulative_energy_drawn", "Cumulative Amp Hours drawn [mAh]", false),
    H7("H7", VeDirectValueType.NUMBER, "min_battery_voltage", "Minimum main (battery) voltage [mV]", false),
    H8("H8", VeDirectValueType.NUMBER, "max_battery_voltage", "Maximum main (battery) voltage [mV]", false),
    H9("H9", VeDirectValueType.NUMBER, "seconds_since_last_full_charge", "Number of seconds since last full charge [sec]", false),
    H10("H10", VeDirectValueType.NUMBER, "number_automatic_synchronizations", "Number of automatic synchronizations", false),
    H11("H11", VeDirectValueType.NUMBER, "number_low_battery_voltage_alarms", "Number of low main voltage alarms", false),
    H12("H12", VeDirectValueType.NUMBER, "number_high_battery_voltage_alarms", "Number of high main voltage alarms", false),
    H13("H13", VeDirectValueType.NUMBER, "number_low_auxiliary_battery_voltage_alarms", "Number of low auxiliary voltage alarms", false),
    H14("H14", VeDirectValueType.NUMBER, "number_high_auxiliary_battery_voltage_alarms", "Number of high auxiliary voltage alarms", false),
    H15("H15", VeDirectValueType.NUMBER, "min_auxiliary_battery_voltage", "Minimum auxiliary (battery) voltage [mV]", false),
    H16("H16", VeDirectValueType.NUMBER, "max_auxiliary_battery_voltage", "Maximum auxiliary (battery) voltage [mV]", false),
    H17("H17", VeDirectValueType.NUMBER, "discharged_energy", "Amount of discharged energy (BMV) / Amount of produced energy (DC monitor) [0.01 kWh]", false),
    H18("H18", VeDirectValueType.NUMBER, "charged_energy", "Amount of charged energy (BMV) / Amount of consumed energy (DC monitor) [0.01 kWh]", false),
    H19("H19", VeDirectValueType.NUMBER, "yield_total", "Yield total (user resettable counter) [0.01 kWh]", false),
    H20("H20", VeDirectValueType.NUMBER, "yield_today", "Yield today [0.01 kWh]", false),
    H21("H21", VeDirectValueType.NUMBER, "max_power_today", "Maximum power today [W]", false),
    H22("H22", VeDirectValueType.NUMBER, "yield_yesterday", "Yield yesterday [0.01 kWh]", false),
    H23("H23", VeDirectValueType.NUMBER, "max_power_yesterday", "Maximum power yesterday [W]", false),
    ERR("ERR", VeDirectValueType.NUMBER, "error_code", "Error code", true),
    CS("CS", VeDirectValueType.NUMBER, "operation_state", "State of operation", true),
    BMV("BMV", VeDirectValueType.TEXT, "bmv_model", "Model description (deprecated)", false),
    FW("FW", VeDirectValueType.TEXT, "firmware_version_16", "Firmware version (16 bit)", false),
    FWE("FWE", VeDirectValueType.TEXT, "firmware_version_24", "Firmware version (24 bit)", false),
    PID("PID", VeDirectValueType.TEXT, "product_id", "Product ID", false),
    SER("SER#", VeDirectValueType.TEXT, "serial_number", "Serial number", false),
    HSDS("HSDS", VeDirectValueType.NUMBER, "day_sequence_number", "Day sequence number (0..364)", false),
    MODE("MODE", VeDirectValueType.NUMBER, "device_mode", "Device mode", true),
    AC_OUT_V("AC_OUT_V", VeDirectValueType.NUMBER, "ac_output_voltage", "AC output voltage [0.01 V]", false),
    AC_OUT_I("AC_OUT_I", VeDirectValueType.NUMBER, "ac_output_current", "AC output current [0.1 A]", false),
    AC_OUT_S("AC_OUT_S", VeDirectValueType.NUMBER, "ac_output_apparent_power", "AC output apparent power [VA]", false),
    WARN("WARN", VeDirectValueType.NUMBER, "warning_reason", "Warning reason", false),
    MPPT("MPPT", VeDirectValueType.NUMBER, "mppt_tracker_operation_mode", "Tracker operation mode", true),
    MON("MON", VeDirectValueType.NUMBER, "dc_monitor_mode", "DC monitor mode", true),
    CAP_BLE("CAP_BLE", VeDirectValueType.HEX, "bluetooth_cap", "Bluetooth capabilities", false);

    companion object {
        fun exists(veName: String) = values().any { it.veName == veName }
        fun resolve(veName: String) = values().first { it.veName == veName }
    }
}

enum class VeDirectOffReason(val code: Long) {
    NONE(0), // 0x00000000
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
