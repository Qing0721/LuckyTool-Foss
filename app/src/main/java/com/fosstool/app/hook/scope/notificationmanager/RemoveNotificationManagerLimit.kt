package com.fosstool.app.hook.scope.notificationmanager

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object RemoveNotificationManagerLimit : YukiBaseHooker() {
    override fun onHook() {
        "com.oplus.notificationmanager.property.uicontroller.ControllerChannelGroup\$AppItemListener"
            .toClassOrNull(appClassLoader)
            ?.method { name = "isSwitchEnabled" }
            ?.ignored()
            ?.hook { replaceToTrue() }
        "com.oplus.notificationmanager.property.uicontroller.ControllerAllowNotificationChannel"
            .toClassOrNull(appClassLoader)
            ?.method { name = "isNormAppEnabled" }
            ?.ignored()
            ?.hook { replaceToTrue() }
        "com.oplus.notificationmanager.property.uicontroller.ControllerUnimportantChannel"
            .toClassOrNull(appClassLoader)
            ?.method { name = "isNormAppEnabled" }
            ?.ignored()
            ?.hook { replaceToTrue() }
        "com.oplus.notificationmanager.property.uicontroller.ControllerAllowNotificationPkg"
            .toClassOrNull(appClassLoader)
            ?.method { name = "isNormAppEnabled" }
            ?.ignored()
            ?.hook { replaceToTrue() }
    }
}
