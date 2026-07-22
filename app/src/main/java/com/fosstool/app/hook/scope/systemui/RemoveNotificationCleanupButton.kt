package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.ModulePrefs

object RemoveNotificationCleanupButton : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("remove_notification_cleanup_button", false)) return

        VariousClass(
            "com.oplusos.systemui.notification.ClearAllController",
            "com.oplus.systemui.statusbar.notification.ClearAllController",
            "com.oplus.systemui.notification.clearall.ClearAllController"
        ).toClass().apply {
            method { name = "setVisible" }.hook {
                before { args().first().setFalse() }
            }
        }
    }
}
