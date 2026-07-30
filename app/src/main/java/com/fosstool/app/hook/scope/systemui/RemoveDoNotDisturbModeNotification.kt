package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object RemoveDoNotDisturbModeNotification : YukiBaseHooker() {
    override fun onHook() {
        VariousClass(
            "com.oplusos.systemui.notification.helper.DndAlertHelper",
            "com.coloros.systemui.notification.helper.DndAlertHelper",
            "com.oplus.systemui.statusbar.notification.helper.DndAlertHelper"
        ).toClassOrNull(appClassLoader)

            ?.method { name = "operateNotification" }?.ignored()?.hook { intercept() }
    }
}
