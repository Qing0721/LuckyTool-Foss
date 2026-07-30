package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog

object RemoveLockScreenCloseNotificationButton : YukiBaseHooker() {
    override fun onHook() {
        val clazz = VariousClass(
            "com.oplusos.systemui.notification.extend.NotificationPanelViewExt",
            "com.oplus.systemui.notification.extend.OplusNotificationCloseButtonImp"
        ).toClassOrNull(appClassLoader)
        if (clazz == null) {
            YLog.error("RemoveLockScreenCloseNotificationButton: target class not found")
            return
        }
        clazz.method { name = "setNotificationCloseButton" }.ignored().hook { intercept() }
    }
}
