package com.fosstool.app.hook.scope.battery

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.injectModuleAppResources
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.TextViewClass
import com.highcapable.yukihookapi.hook.type.android.ViewClass
import com.fosstool.app.R
import com.fosstool.app.hook.utils.calcLocalBatteryHealth
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.filterNumber
import com.fosstool.app.utils.safeOf

object BatteryHealthDataTips : YukiBaseHooker() {
    @SuppressLint("DiscouragedApi", "SetTextI18n")
    override fun onHook() {
        "com.oplus.powermanager.fuelgaue.BatteryHealthDataPreference".toClass().apply {
            method { param(ViewClass) }.hook {
                after {
                    val view = args().first().cast<View>() ?: return@after
                    val context = view.context
                    context.injectModuleAppResources()
                    val content = view.findViewById<TextView>(
                        view.resources.getIdentifier(
                            "max_capacity_content",
                            "id", this@BatteryHealthDataTips.packageName
                        )
                    ) ?: return@after
                    val data = field { type = TextViewClass }.get(instance).cast<TextView>()
                        ?: return@after

                    val customPct = prefs(ModulePrefs)
                        .getString("customize_battery_health_data_percentage", "") ?: ""
                    val showCalc = prefs(ModulePrefs)
                        .getBoolean("display_module_calculates_battery_health_data", false)
                    val fixTips = prefs(ModulePrefs)
                        .getBoolean("fix_battery_health_data_display", false)

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
                        content.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                        if (content.text.length == 1) {
                            content.text = "${content.text}$calcLabel"
                        }
                        data.layoutParams?.let { it.width = ViewGroup.LayoutParams.WRAP_CONTENT }
                        data.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                        if (data.text.length == 1) {
                            data.text = "${data.text}$actualHealth%"
                        }
                    }

                    if (fixTips) {
                        val num = data.text.filterNumber.toIntOrNull()
                        val tipStr =
                            context.getString(R.string.fix_battery_health_data_display_tips)
                        val tips = if (num == null) "${tipStr}\n" else ""
                        content.apply {
                            gravity = Gravity.CENTER
                            text = "$text\n${tips}By: LuckyTool"
                        }
                    }
                }
            }
        }
    }
}
