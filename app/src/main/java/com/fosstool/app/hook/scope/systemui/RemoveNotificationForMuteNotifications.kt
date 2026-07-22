package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method

object RemoveNotificationForMuteNotifications : YukiBaseHooker() {
    override fun onHook() {
        VariousClass(
            "com.oplusos.systemui.statusbar.controller.NoDisturbController",
            "com.oplus.systemui.statusbar.controller.NoDisturbController"
        ).toClass().apply {
            method { name = "sendNotification" }.hook {
                intercept()
            }
        }
    }
}
