package com.fosstool.app.hook.scope.systemui

import android.graphics.Typeface
import android.util.TypedValue
import android.widget.TextView
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.A14
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK

object StatusBarPower : YukiBaseHooker() {
    override fun onHook() {
        if (SDK >= A14) loadHooker(StatusBarPowerStyle)
        else loadHooker(StatusBarPowerStyleC13)
    }

    object StatusBarPowerStyle : YukiBaseHooker() {
        override fun onHook() {
            val removePercent =
                prefs(ModulePrefs).getBoolean("remove_statusbar_battery_percent", false)
            val userTypeface = prefs(ModulePrefs).getBoolean("statusbar_power_user_typeface", false)
            val boldTypeface =
                prefs(ModulePrefs).getBoolean("statusbar_power_bold_typeface", false) ||
                    prefs(ModulePrefs).getBoolean("statusbar_power_use_bold_font_style", false)
            val customFontSize = prefs(ModulePrefs).getInt("statusbar_power_font_size", 0)

            "com.oplus.systemui.statusbar.pipeline.battery.ui.binder.BatteryViewBinder".toClass()
                .apply {
                    method { name = "bind\$initView" }.hook {
                        after {
                            args(1).cast<TextView>()?.apply {
                                if (removePercent) text = text.toString().replace("%", "")
                                if (userTypeface) {
                                    typeface = if (boldTypeface) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                                    setTextSize(
                                        TypedValue.COMPLEX_UNIT_DIP,
                                        if (customFontSize == 0) 12F else customFontSize.toFloat() * 2
                                    )
                                }
                            }
                        }
                    }
                }
        }
    }

    object StatusBarPowerStyleC13 : YukiBaseHooker() {
        override fun onHook() {
            val removePercent =
                prefs(ModulePrefs).getBoolean("remove_statusbar_battery_percent", false)
            val userTypeface = prefs(ModulePrefs).getBoolean("statusbar_power_user_typeface", false)
            val powerApplyToBatteryIcon =
                prefs(ModulePrefs).getBoolean("statusbar_power_apply_to_battery_icon", false)
            val boldTypeface =
                prefs(ModulePrefs).getBoolean("statusbar_power_bold_typeface", false) ||
                    prefs(ModulePrefs).getBoolean("statusbar_power_use_bold_font_style", false)
            val customFontSize = prefs(ModulePrefs).getInt("statusbar_power_font_size", 0)

            "com.oplusos.systemui.statusbar.widget.StatBatteryMeterView".toClass().apply {
                method { name = "onConfigChanged" }.hook {
                    after {
                        method { name = "updatePercentText" }.get(instance).call()
                    }
                }
                method { name = "updatePercentText" }.hook {
                    after {
                        field { name = "batteryPercentText" }.get(instance).cast<TextView>()
                            ?.apply {
                                if (removePercent) text = text.toString().replace("%", "")
                                if (userTypeface || powerApplyToBatteryIcon) {
                                    typeface = if (boldTypeface) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                                    setTextSize(
                                        TypedValue.COMPLEX_UNIT_DIP,
                                        if (customFontSize == 0) 12F else customFontSize.toFloat() * 2
                                    )
                                }
                            }
                    }
                }
            }
        }
    }
}
