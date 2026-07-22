package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.systemui.AllowLongPressNotificationModifiable
import com.fosstool.app.utils.ModulePrefs

object StatusBarNotifiyLimit : YukiBaseHooker() {
    override fun onHook() {
        if (prefs(ModulePrefs).getBoolean("allow_long_press_notification_modifiable", false)) {
            loadHooker(AllowLongPressNotificationModifiable)
        }
    }
}
