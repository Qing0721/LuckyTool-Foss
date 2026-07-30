package com.fosstool.app.hook.scope.uiengine

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object RemoveAodNotificationWhitelist : YukiBaseHooker() {
    override fun onHook() {
        "com.oplus.egview.widget.BaseView".toClassOrNull(appClassLoader)
            ?.method { name = "isExpRegion" }
            ?.ignored()
            ?.hook { replaceToTrue() }
    }
}
