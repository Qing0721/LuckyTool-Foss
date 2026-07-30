package com.fosstool.app.hook.hooker

import com.fosstool.app.hook.scope.aod.AodRandomTextAndTypeface
import com.fosstool.app.hook.scope.uiengine.RemoveAodNotificationWhitelist
import com.fosstool.app.hook.scope.uiengine.SetAodStyleMode
import com.fosstool.app.hook.utils.OplusBuildUtlils
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker

object HookUIEngine : YukiBaseHooker() {
    override fun onHook() {
        if (SDK < A13) return

        if (prefs(ModulePrefs).getBoolean("remove_aod_notification_icon_whitelist", false) && SDK == A13) {
            loadHooker(RemoveAodNotificationWhitelist)
        }

        loadHooker(SetAodStyleMode)

        val os = try {
            OplusBuildUtlils(appClassLoader).getOSVersionCode ?: 0
        } catch (_: Throwable) {
            0
        }
        if (os >= 26) {
            loadHooker(AodRandomTextAndTypeface)
        }
    }
}
