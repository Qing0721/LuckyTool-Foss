package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.A14
import com.fosstool.app.utils.SDK

object RemoveGTModeNotification : YukiBaseHooker() {
    override fun onHook() {
        VariousClass(
            "com.oplusos.systemui.statusbar.util.GTUtils",
            "com.oplus.systemui.statusbar.util.GTUtils"
        ).toClass().apply {
            method {
                name = if (SDK >= A14) "notifyOpenGtMode"
                else "showOpenGtModeNotify"
            }.hook { intercept() }
        }
    }
}
