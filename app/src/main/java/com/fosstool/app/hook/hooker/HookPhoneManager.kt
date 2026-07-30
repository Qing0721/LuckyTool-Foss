package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.phonemanager.RemoveCountdownAddVirusAppWhitelist
import com.fosstool.app.hook.scope.phonemanager.RemoveVirusRiskNotificationInPhoneManager
import com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker
import com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker.Mode
import com.fosstool.app.utils.ModulePrefs

object HookPhoneManager : YukiBaseHooker() {
    override fun onHook() {
        loadHooker(SystemPropertiesOverrideEngineHooker(mode = Mode.RM0_Q))

        if (
            prefs(ModulePrefs).getBoolean("remove_virus_risk_notification_in_phone_manager", false)
        ) {
            loadHooker(RemoveVirusRiskNotificationInPhoneManager)
        }
        if (prefs(ModulePrefs).getBoolean("remove_countdown_add_virus_app_whitelist", false)) {
            loadHooker(RemoveCountdownAddVirusAppWhitelist)
        }
    }
}
