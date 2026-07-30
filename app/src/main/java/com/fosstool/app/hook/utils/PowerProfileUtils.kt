package com.fosstool.app.hook.utils

import android.content.Context
import com.highcapable.yukihookapi.hook.factory.buildOf
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.SDK
import com.fosstool.app.utils.safeOfNull
import java.io.BufferedReader
import java.io.FileReader
import kotlin.math.roundToInt

@Suppress("unused")
class PowerProfileUtils(val classLoader: ClassLoader?) {

    val clazz = "com.android.internal.os.PowerProfile".toClassOrNull(classLoader)

    fun buildInstance(context: Context?): Any? {
        return clazz?.buildOf(context) {
            param(ContextClass)
        }
    }

    fun getBatteryCapacity(instance: Any?): Double? {
        return clazz?.method {
            name = "getBatteryCapacity"
        }?.get(instance)?.invoke<Double>()
    }

}

fun calcLocalBatteryHealth(context: Context?, classLoader: ClassLoader? = null): Int {
    if (context == null) return -1
    val sohFile = if (SDK >= A13) "/sys/class/oplus_chg/battery/battery_soh"
    else "/sys/class/power_supply/battery/batt_soh"
    val fccFile = if (SDK >= A13) "/sys/class/oplus_chg/battery/battery_fcc"
    else "/sys/class/power_supply/battery/batt_fcc"

    val sohValue = safeOfNull {
        BufferedReader(FileReader(sohFile)).readLine()?.trim()?.split(" ")?.firstOrNull()?.toIntOrNull()
    }
    if (sohValue != null && sohValue in 1..100) return sohValue

    val fccValue = safeOfNull {
        BufferedReader(FileReader(fccFile)).readLine()?.trim()?.split(" ")?.firstOrNull()?.toFloatOrNull()
    } ?: return -1
    if (fccValue <= 0) return -1

    val powerIns = PowerProfileUtils(classLoader).buildInstance(context) ?: return -1
    val designCapacity = PowerProfileUtils(classLoader).getBatteryCapacity(powerIns) ?: return -1
    val calc = (fccValue / designCapacity * 100.0).roundToInt()
    return if (calc > 100) calc / 1000 else calc
}
