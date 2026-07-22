package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method

object RemoveLowBatteryDialogWarning : YukiBaseHooker() {
    override fun onHook() {
        VariousClass(
            "com.oplusos.systemui.notification.power.OplusPowerNotificationWarnings",
            "com.oplus.systemui.statusbar.notification.power.OplusPowerNotificationWarnings"
        ).toClass().apply {
            method { name = "createSavePowerDialog" }.hook {
                intercept()
            }
            method { name = "createSuperSavePowerDialog" }.hook {
                intercept()
            }
        }
    }
}
