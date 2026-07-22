package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.battery.BatteryFeatureProvider
import com.fosstool.app.hook.scope.battery.BatteryHealthDataTips
import com.fosstool.app.hook.scope.battery.HookBatteryNotify
import com.fosstool.app.hook.scope.battery.RemoveBatteryRestrictPlugin
import com.fosstool.app.hook.scope.battery.RemoveBatteryTemperatureControl
import com.fosstool.app.hook.scope.battery.UnlockStartupLimit
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK

object HookBattery : YukiBaseHooker() {
    override fun onHook() {
        loadHooker(BatteryFeatureProvider)

        loadHooker(HookBatteryNotify)

        if (prefs(ModulePrefs).getBoolean("unlock_startup_limit", false)) {
            if (SDK >= A13) loadHooker(UnlockStartupLimit)
        }

        if (prefs(ModulePrefs).getBoolean("remove_battery_temperature_control", false)) {
            loadHooker(RemoveBatteryTemperatureControl)
        }

        if (prefs(ModulePrefs).getBoolean("remove_battery_restrict_plugin", false)) {
            loadHooker(RemoveBatteryRestrictPlugin)
        }

        if (SDK >= A13 && (
                prefs(ModulePrefs).getBoolean("fix_battery_health_data_display", false) ||
                    prefs(ModulePrefs).getBoolean("display_module_calculates_battery_health_data", false) ||
                    (prefs(ModulePrefs).getString("customize_battery_health_data_percentage", "") ?: "").isNotEmpty()
                )
        ) {
            loadHooker(BatteryHealthDataTips)
        }


    }
}
