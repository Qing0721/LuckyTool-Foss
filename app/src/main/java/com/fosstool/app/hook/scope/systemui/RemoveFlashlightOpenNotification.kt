package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method

object RemoveFlashlightOpenNotification : YukiBaseHooker() {
    override fun onHook() {
        VariousClass(
            "com.oplusos.systemui.flashlight.FlashlightNotification",
            "com.oplus.systemui.statusbar.notification.flashlight.FlashlightNotification"
        ).toClass().apply {
            method { name = "sendNotification";paramCount = 1 }.hook {
                intercept()
            }
        }
    }
}
