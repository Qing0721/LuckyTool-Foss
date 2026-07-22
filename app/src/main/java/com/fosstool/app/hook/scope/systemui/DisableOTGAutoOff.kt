package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method

object DisableOTGAutoOff : YukiBaseHooker() {
    override fun onHook() {
        VariousClass(
            "com.oplusos.systemui.notification.helper.OtgHelper",
            "com.oplus.systemui.qs.helper.OtgHelper"
        ).toClass().apply {
            method { name = "setAutoCloseAlarm" }.hook {
                after {
                    method { name = "cancelAutoCloseAlarm" }.get(instance).call()
                }
            }
        }
    }
}
