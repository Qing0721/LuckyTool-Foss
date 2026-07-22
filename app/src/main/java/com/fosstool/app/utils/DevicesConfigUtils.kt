package com.fosstool.app.utils

import org.json.JSONObject

@Suppress("MemberVisibilityCanBePrivate", "unused")
object DevicesConfigUtils {

    fun getDevicesConfig(): JSONObject? {
        val json =
            ShellUtils.execCommand("cat /odm/etc/devices_config/devices_config.json", false, true)
                .let {
                    if (it.result == 1) null
                    else if (it.successMsg.isBlank()) null
                    else it.successMsg
                }
        return json?.let { JSONObject(it) }
    }

    fun getChargeConfig(): JSONObject? {
        return getDevicesConfig()?.getJSONObject("charge")
    }

    fun getOTGConfig(): JSONObject? {
        return getDevicesConfig()?.getJSONObject("otg")
    }

    fun getShouderConfig(): JSONObject? {
        return getDevicesConfig()?.getJSONObject("shouder")
    }

    fun getUSBConfig(): JSONObject? {
        return getDevicesConfig()?.getJSONObject("usb")
    }

    fun getVibratorConfig(): JSONObject? {
        return getDevicesConfig()?.getJSONObject("vibrator")
    }

    fun getWhiteLightConfig(): JSONObject? {
        return getDevicesConfig()?.getJSONObject("lights_white")
    }

    fun getWirelessChargeConfig(): JSONObject? {
        return getDevicesConfig()?.getJSONObject("wireless_charge")
    }

    val isSeriesDualBattery
        get() : Boolean? = safeOfNull {
            getChargeConfig()?.getBoolean("series_dual_battery_support")
        }

    val isParallelDualBattery
        get() : Boolean? = safeOfNull {
            getChargeConfig()?.getBoolean("parallel_dual_battery_support")
        }

    val isVBatDeviation
        get() : Boolean? = safeOfNull {
            getChargeConfig()?.getBoolean("qg_vbat_deviation_support")
        }

    val isAirSVOOCSupport
        get() = safeOfNull {
            getWirelessChargeConfig()?.getBoolean("air_svooc_support")
        }

    val isAirVOOCSupport
        get() = safeOfNull {
            getWirelessChargeConfig()?.getBoolean("air_vooc_support")
        }
}
