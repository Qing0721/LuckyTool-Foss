package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.ModulePrefs

object EnableNotificationBackgroundBlurEffect : YukiBaseHooker() {
    override fun onHook() {
        val blur = prefs(ModulePrefs).getBoolean("enable_notification_background_blur_effect", false)
        if (!blur) return

        "com.android.systemui.statusbar.notification.row.NotificationBackgroundView".toClass().apply {
            method { name = "draw" }.hook {
                before { resultNull() }
            }
        }
    }
}
