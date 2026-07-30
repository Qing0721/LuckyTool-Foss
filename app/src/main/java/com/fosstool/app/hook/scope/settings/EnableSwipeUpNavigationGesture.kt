package com.fosstool.app.hook.scope.settings

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object EnableSwipeUpNavigationGesture : YukiBaseHooker() {
    override fun onHook() {
        "com.oplus.settings.feature.navbar.NavBarSettingsValueUtil".toClassOrNull(appClassLoader)
            ?.method { name = "getGestureUpModeAvailable"; paramCount = 1 }
            ?.ignored()
            ?.hook { replaceTo(0) }
    }
}
