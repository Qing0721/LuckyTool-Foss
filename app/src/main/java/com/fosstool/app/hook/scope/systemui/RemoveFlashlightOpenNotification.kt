package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object RemoveFlashlightOpenNotification : YukiBaseHooker() {
    override fun onHook() {

        VariousClass(
            "com.oplusos.systemui.flashlight.FlashlightNotification",
            "com.oplus.systemui.statusbar.notification.flashlight.FlashlightNotification",
            "com.oplus.systemui.notification.flashlight.FlashlightNotification"
        ).toClassOrNull(appClassLoader)
            ?.method { name = "sendNotification"; paramCount = 1 }?.ignored()?.hook { intercept() }
    }
}
