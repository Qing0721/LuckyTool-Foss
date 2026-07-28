package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object RemoveLowBatteryDialogWarning : YukiBaseHooker() {
    override fun onHook() {
        VariousClass(
            "com.oplusos.systemui.notification.power.OplusPowerNotificationWarnings",
            "com.oplus.systemui.statusbar.notification.power.OplusPowerNotificationWarnings"
        ).toClassOrNull(appClassLoader)?.let { c ->
            c.method { name = "createSavePowerDialog" }.ignored().hook { intercept() }
            c.method { name = "createSuperSavePowerDialog" }.ignored().hook { intercept() }

            c.method { name = "showLowBatteryWarning" }.ignored().hook { replaceToFalse() }
        }
    }
}
