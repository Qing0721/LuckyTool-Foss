package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object DisableOTGAutoOff : YukiBaseHooker() {
    override fun onHook() {

        VariousClass(
            "com.oplusos.systemui.notification.helper.OtgHelper",
            "com.oplus.systemui.qs.helper.OtgHelper"
        ).toClassOrNull(appClassLoader)
            ?.method { name = "setAutoCloseAlarm" }?.ignored()?.hook { intercept() }
    }
}
