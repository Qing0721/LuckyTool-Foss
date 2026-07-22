package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.uiengine.RemoveAodNotificationWhitelist
import com.fosstool.app.hook.scope.uiengine.SetAodStyleMode
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK

object HookUIEngine : YukiBaseHooker() {
    override fun onHook() {
        if (SDK != A13) return

        if (prefs(ModulePrefs).getBoolean("remove_aod_notification_icon_whitelist", false)) {
            loadHooker(RemoveAodNotificationWhitelist)
        }

        loadHooker(SetAodStyleMode)
    }
}
