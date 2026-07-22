package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.hasField
import com.highcapable.yukihookapi.hook.factory.method

object AllowLongPressNotificationModifiable : YukiBaseHooker() {
    override fun onHook() {
        VariousClass(
            "com.oplusos.systemui.notification.settingspanel.NotificationSettingsModel",
            "com.oplusos.systemui.notification.settingspanel.controller.NotificationController",
            "com.oplus.systemui.statusbar.notification.settingspanel.controller.NotificationController"
        ).toClass().apply {
            method {
                name = when (simpleName) {
                    "NotificationSettingsModel" -> "resolveMode"
                    "NotificationController" -> "resolveSettingsModel"
                    else -> "resolveMode"
                }
                paramCount = 1
            }.hook {
                before {
                    val hasField = instance.javaClass.hasField { name = "isAppModifiable" }
                    if (hasField) field { name = "isAppModifiable" }.get(instance).setTrue()
                    else args().first().any()?.current()?.field { name = "isAppModifiable" }
                        ?.setTrue()
                }
            }
        }
    }
}
