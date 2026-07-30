package com.fosstool.app.hook.hooker

import com.fosstool.app.hook.scope.notificationmanager.ForceDisplayClockStyleOptions
import com.fosstool.app.hook.scope.notificationmanager.RemoveNotificationManagerLimit
import com.fosstool.app.hook.scope.notificationmanager.RemoveNotificationPinNumberLimit
import com.fosstool.app.hook.utils.OplusBuildUtlils
import com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker
import com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker.Mode
import com.fosstool.app.utils.A14
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker

object HookNotificationManager : YukiBaseHooker() {
    override fun onHook() {
        loadHooker(SystemPropertiesOverrideEngineHooker(mode = Mode.RM0_Q))

        if (prefs(ModulePrefs).getBoolean("remove_notification_manager_limit", false)) {
            loadHooker(RemoveNotificationManagerLimit)
        }

        val os = try {
            OplusBuildUtlils(appClassLoader).getOSVersionCode ?: 0
        } catch (_: Throwable) {
            0
        }
        if (prefs(ModulePrefs).getBoolean("remove_notification_pin_number_limit", false) && os >= 33) {
            loadHooker(RemoveNotificationPinNumberLimit)
        }

        if (prefs(ModulePrefs).getBoolean("force_display_clock_style_options", false) && SDK == A14) {
            loadHooker(ForceDisplayClockStyleOptions)
        }
    }
}
