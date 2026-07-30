package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.utils.ModulePrefs

object RemoveNotificationCleanupButton : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("remove_notification_cleanup_button", false)) return

        VariousClass(
            "com.oplusos.systemui.notification.ClearAllController",
            "com.oplus.systemui.statusbar.notification.ClearAllController",
            "com.oplus.systemui.notification.clearall.ClearAllController"
        ).toClassOrNull(appClassLoader)

            ?.method { name = "setVisible"; paramCount = 3 }?.ignored()?.hook {
                before {
                    if (args.isNotEmpty()) args[0] = false
                }
            }
    }
}
