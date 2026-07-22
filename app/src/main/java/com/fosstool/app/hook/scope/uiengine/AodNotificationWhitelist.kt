package com.fosstool.app.hook.scope.uiengine

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method

object RemoveAodNotificationWhitelist : YukiBaseHooker() {
    override fun onHook() {
        "com.oplus.egview.widget.BaseView".toClass().apply {
            method { name = "isExpRegion" }.hook {
                replaceToTrue()
            }
        }
    }
}
