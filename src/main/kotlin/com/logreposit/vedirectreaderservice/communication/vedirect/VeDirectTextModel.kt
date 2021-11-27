package com.logreposit.vedirectreaderservice.communication.vedirect

abstract class VeDirectReading<out T>(private val field: VeDirectField, private val value: T) {
    fun getValue(): T = value
}

class VeDirectNumberReading(field: VeDirectField, value: Long) : VeDirectReading<Long>(field, value)
class VeDirectOnOffReading(field: VeDirectField, value: Boolean) : VeDirectReading<Boolean>(field, value)
class VeDirectTextReading(field: VeDirectField, value: String) : VeDirectReading<String>(field, value)

//sealed class VeDirectReading(private val field: VeDirectField)
//
//class VeDirectNumberReading(field: VeDirectField, private val value: Long) : VeDirectReading(field)
//class VeDirectOnOffReading(field: VeDirectField, private val value: Boolean) : VeDirectReading(field)
//class VeDirectTextReading(field: VeDirectField, private val value: String) : VeDirectReading(field)

enum class VeDirectField(val veName: String, val valueType: VeDirectValueType, logrepositName: String) {
    V("V", VeDirectValueType.NUMBER, "battery_voltage"),
    V2("V2", VeDirectValueType.NUMBER, "battery_voltage_2"),
    V3("V3", VeDirectValueType.NUMBER, "battery_voltage_3"),
    VS("VS", VeDirectValueType.NUMBER, "auxiliary_voltage"),
    VM("VM", VeDirectValueType.NUMBER, "mid_point_battery_voltage"),
    DM("DM", VeDirectValueType.NUMBER, "mid_point_battery_deviation"), // TODO: maybe `mid_point_battery_voltage_deviation ??`
    VPV("VPV", VeDirectValueType.NUMBER, "panel_voltage"),
    PPV("PPV", VeDirectValueType.NUMBER, "panel_power"),
    I("I", VeDirectValueType.NUMBER, "battery_current"),
    I2("I2", VeDirectValueType.NUMBER, "battery_current_2"),
    I3("I3", VeDirectValueType.NUMBER, "battery_current_3"),
    IL("IL", VeDirectValueType.NUMBER, "load_current"),
    LOAD("LOAD", VeDirectValueType.ON_OFF, "load_output_state"),
    T("T", VeDirectValueType.NUMBER, "battery_temperature"),
    P("P", VeDirectValueType.NUMBER, ""), // TODO: find a name!
    CE("CE", VeDirectValueType.NUMBER, "consumed_energy"),
    SOC("SOC", VeDirectValueType.NUMBER, "state_of_charge"),
    TTG("TTG", VeDirectValueType.NUMBER, "time_to_go"),
    ALARM("Alarm", VeDirectValueType.ON_OFF, "alarm_state"),
    RELAY("Relay", VeDirectValueType.ON_OFF, "relay_state"),
    AR("AR", VeDirectValueType.NUMBER, "alarm_reason"),
    OR("OR", VeDirectValueType.HEX, "off_reason"),
    H1("H1", VeDirectValueType.NUMBER, "max_discharge_depth"),
    H2("H2", VeDirectValueType.NUMBER, "last_discharge_depth"),
    H3("H3", VeDirectValueType.NUMBER, "average_discharge_depth"),
    H4("H4", VeDirectValueType.NUMBER, "number_charge_cycles"),
    H5("H5", VeDirectValueType.NUMBER, "number_full_discharges"),
    H6("H6", VeDirectValueType.NUMBER, "cumulative_energy_drawn"),
    H7("H7", VeDirectValueType.NUMBER, "min_battery_voltage"),
    H8("H8", VeDirectValueType.NUMBER, "max_battery_voltage"),
    H9("H9", VeDirectValueType.NUMBER, "seconds_since_last_full_charge"),
    H10("H10", VeDirectValueType.NUMBER, "number_automatic_synchronizations"),
    H11("H11", VeDirectValueType.NUMBER, "number_low_battery_voltage_alarms"),
    H12("H12", VeDirectValueType.NUMBER, "number_high_battery_voltage_alarms"),
    H13("H13", VeDirectValueType.NUMBER, "number_low_auxiliary_battery_voltage_alarms"),
    H14("H14", VeDirectValueType.NUMBER, "number_high_auxiliary_battery_voltage_alarms"),
    H15("H15", VeDirectValueType.NUMBER, "min_auxiliary_battery_voltage"),
    H16("H16", VeDirectValueType.NUMBER, "max_auxiliary_battery_voltage"),
    H17("H17", VeDirectValueType.NUMBER, ""), // TODO: find a name!
    H18("H18", VeDirectValueType.NUMBER, ""), // TODO: find a name!
    H19("H19", VeDirectValueType.NUMBER, "yield_total"), // TODO: find a better name?
    H20("H20", VeDirectValueType.NUMBER, "yield_today"), // TODO: find a better name?
    H21("H21", VeDirectValueType.NUMBER, "max_power_today"), // TODO: find a better name?
    H22("H22", VeDirectValueType.NUMBER, "yield_yesterday"), // TODO: find a better name?
    H23("H23", VeDirectValueType.NUMBER, "max_power_yesterday"), // TODO: find a better name?
    ERR("ERR", VeDirectValueType.NUMBER, "error_code"),
    CS("CS", VeDirectValueType.NUMBER, "operation_state"),
    BMV("BMV", VeDirectValueType.TEXT, "bmv_model"), // deprecated
    FW("FW", VeDirectValueType.TEXT, "firmware_version_16"), // text
    FWE("FWE", VeDirectValueType.TEXT, "firmware_version_32"), // text
    PID("PID", VeDirectValueType.TEXT, "product_id"), // text
    SER("SER#", VeDirectValueType.TEXT, "serial_number"),
    HSDS("HSDS", VeDirectValueType.NUMBER, "day_sequence_number"),
    MODE("MODE", VeDirectValueType.NUMBER, "device_mode"),
    AC_OUT_V("AC_OUT_V", VeDirectValueType.NUMBER, "ac_output_voltage"),
    AC_OUT_I("AC_OUT_I", VeDirectValueType.NUMBER, "ac_output_current"),
    AC_OUT_S("AC_OUT_S", VeDirectValueType.NUMBER, "ac_output_apparent_power"),
    WARN("WARN", VeDirectValueType.NUMBER, "warning_reason"),
    MPPT("MPPT", VeDirectValueType.NUMBER, "mppt_tracker_operation_mode"),
    MON("MON", VeDirectValueType.NUMBER, "dc_monitor_mode"),
    CAP_BLE("CAP_BLE", VeDirectValueType.HEX, "cap_ble"); // TODO: find a name!

    companion object {
        fun exists(veName: String) = values().any { it.veName == veName }
        fun resolve(veName: String) = values().first() { it.veName == veName }
    }
}

enum class VeDirectValueType {
    NUMBER,
    ON_OFF,
    TEXT,
    HEX
}
