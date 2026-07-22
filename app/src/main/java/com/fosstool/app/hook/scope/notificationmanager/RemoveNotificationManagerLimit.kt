package com.fosstool.app.hook.scope.notificationmanager

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method

object RemoveNotificationManagerLimit : YukiBaseHooker() {
    override fun onHook() {
        "com.oplus.notificationmanager.property.uicontroller.ControllerChannelGroup\$AppItemListener".toClass()
            .apply {
                method { name = "isSwitchEnabled" }.hook {
                    replaceToTrue()
                }
            }
        "com.oplus.notificationmanager.property.uicontroller.ControllerAllowNotificationChannel".toClass()
            .apply {
                method { name = "isNormAppEnabled" }.hook {
                    replaceToTrue()
                }
            }
        "com.oplus.notificationmanager.property.uicontroller.ControllerUnimportantChannel".toClass()
            .apply {
                method { name = "isNormAppEnabled" }.hook {
                    replaceToTrue()
                }
            }
        "com.oplus.notificationmanager.property.uicontroller.ControllerAllowNotificationPkg".toClass()
            .apply {
                method { name = "isNormAppEnabled" }.hook {
                    replaceToTrue()
                }
            }
    }
}
