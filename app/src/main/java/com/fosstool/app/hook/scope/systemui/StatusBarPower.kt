package com.fosstool.app.hook.scope.systemui

import android.graphics.Typeface
import android.util.TypedValue
import android.widget.TextView
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.utils.A14
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Field

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

            "com.oplus.systemui.statusbar.pipeline.battery.ui.binder.BatteryViewBinder"
                .toClassOrNull(appClassLoader)
                ?.method { name = "bind\$initView" }?.ignored()?.hook {
                    after {
                        val tv = args.getOrNull(1) as? TextView ?: return@after
                        if (removePercent) tv.text = tv.text.toString().replace("%", "")
                        if (userTypeface) {
                            tv.typeface =
                                if (boldTypeface) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                            tv.setTextSize(
                                TypedValue.COMPLEX_UNIT_DIP,
                                if (customFontSize == 0) 12F else customFontSize.toFloat() * 2
                            )
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

            "com.oplusos.systemui.statusbar.widget.StatBatteryMeterView"
                .toClassOrNull(appClassLoader)?.let { c ->
                    c.method { name = "onConfigChanged" }.ignored().hook {
                        after {
                            runCatching {
                                XposedHelpers.callMethod(instance, "updatePercentText")
                            }
                        }
                    }
                    c.method { name = "updatePercentText" }.ignored().hook {
                        after {
                            val tv =
                                c.findField("batteryPercentText")?.get(instance) as? TextView
                                    ?: return@after
                            if (removePercent) tv.text = tv.text.toString().replace("%", "")
                            if (userTypeface || powerApplyToBatteryIcon) {
                                tv.typeface =
                                    if (boldTypeface) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                                tv.setTextSize(
                                    TypedValue.COMPLEX_UNIT_DIP,
                                    if (customFontSize == 0) 12F else customFontSize.toFloat() * 2
                                )
                            }
                        }
                    }
                }
        }
    }

    private fun Class<*>.findField(name: String): Field? {
        var cls: Class<*>? = this
        while (cls != null) {
            runCatching { return cls.getDeclaredField(name).also { it.isAccessible = true } }
            cls = cls.superclass
        }
        return null
    }
}
