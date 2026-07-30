package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object RemoveChargingCompleted : YukiBaseHooker() {
    override fun onHook() {
        VariousClass(
            "com.coloros.systemui.notification.power.ColorosPowerNotificationWarnings",
            "com.oplusos.systemui.notification.power.OplusPowerNotificationWarnings",
            "com.coloros.systemui.notification.power.ColorosPowerNotificationWarnings",
            "com.oplus.systemui.statusbar.notification.power.OplusPowerNotificationWarnings"
        ).toClassOrNull(appClassLoader)
            ?.method { name = "showChargeErrorDialog"; paramCount = 1 }?.ignored()?.hook {
                before {
                    if ((args.getOrNull(0) as? Int) == 7) result = null
                }
            }
    }
}
