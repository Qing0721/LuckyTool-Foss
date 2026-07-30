package com.fosstool.app.hook.scope.battery

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.fosstool.app.R
import com.fosstool.app.hook.utils.calcLocalBatteryHealth
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.filterNumber
import com.fosstool.app.utils.safeOf
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.injectModuleAppResources
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge

object BatteryHealthDataTips : YukiBaseHooker() {
    @SuppressLint("DiscouragedApi", "SetTextI18n")
    override fun onHook() {
        val clazz = "com.oplus.powermanager.fuelgaue.BatteryHealthDataPreference".toClassOrNull(appClassLoader) ?: return
        val target = clazz.declaredMethods.firstOrNull {
            it.parameterCount == 1 && View::class.java.isAssignableFrom(it.parameterTypes[0])
        } ?: return
        runCatching {
            XposedBridge.hookMethod(target, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val view = param.args.getOrNull(0) as? View ?: return
                    val context = view.context
                    context.injectModuleAppResources()
                    val content = view.findViewById<TextView>(
                        view.resources.getIdentifier(
                            "max_capacity_content",
                            "id",
                            this@BatteryHealthDataTips.packageName,
                        ),
                    ) ?: return
                    val data = clazz.declaredFields
                        .firstOrNull { TextView::class.java.isAssignableFrom(it.type) }
                        ?.apply { isAccessible = true }
                        ?.get(param.thisObject) as? TextView
                        ?: return

                    val customPct = prefs(ModulePrefs)
                        .getString("customize_battery_health_data_percentage", "") ?: ""
                    val showCalc = prefs(ModulePrefs)
                        .getBoolean("display_module_calculates_battery_health_data", false)

                    val digits = customPct.filterNumber
                    if (digits.isNotEmpty()) {
                        data.text = "$digits%"
                    }

                    if (showCalc) {
                        val actualHealth = calcLocalBatteryHealth(context, appClassLoader)
                        val calcLabel = safeOf(default = " Calc") {
                            context.getString(R.string.calculated_maximum_capacity)
                        }
                        content.layoutParams?.let { it.width = ViewGroup.LayoutParams.WRAP_CONTENT }

                        content.gravity = Gravity.START

                        if (content.text.lines().size == 1) {
                            content.text = "${content.text}$calcLabel"
                        }
                        data.layoutParams?.let { it.width = ViewGroup.LayoutParams.WRAP_CONTENT }

                        data.gravity = Gravity.END
                        if (data.text.lines().size == 1) {
                            data.text = "${data.text}$actualHealth%"
                        }
                    }
                }
            })
        }
    }
}
