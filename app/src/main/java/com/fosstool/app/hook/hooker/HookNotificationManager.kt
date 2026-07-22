package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.notificationmanager.RemoveNotificationManagerLimit
import com.fosstool.app.hook.scope.notificationmanager.RemoveNotificationPinNumberLimit
import com.fosstool.app.utils.ModulePrefs

object HookNotificationManager : YukiBaseHooker() {
    override fun onHook() {
        if (prefs(ModulePrefs).getBoolean("remove_notification_manager_limit", false)) {
            loadHooker(RemoveNotificationManagerLimit)
        }

        if (prefs(ModulePrefs).getBoolean("remove_notification_pin_number_limit", false)) {
            loadHooker(RemoveNotificationPinNumberLimit)
        }

    }
}
