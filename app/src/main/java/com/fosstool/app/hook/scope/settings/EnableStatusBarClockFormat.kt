package com.fosstool.app.hook.scope.settings

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object EnableStatusBarClockFormat : YukiBaseHooker() {
    override fun onHook() {
        "com.oplus.settings.feature.notification.controller.RmStatusbarClockPreferenceController"
            .toClassOrNull(appClassLoader)
            ?.method { name = "getAvailabilityStatus" }
            ?.ignored()
            ?.hook { replaceTo(0) }
    }
}
