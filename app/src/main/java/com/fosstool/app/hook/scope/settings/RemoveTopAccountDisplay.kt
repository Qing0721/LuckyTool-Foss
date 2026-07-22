package com.fosstool.app.hook.scope.settings

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.SDK

object RemoveTopAccountDisplay : YukiBaseHooker() {
    override fun onHook() {
        "com.oplus.settings.feature.homepage.user.UserPreferenceController".toClass().apply {
            method {
                name = if (SDK >= A13) "checkAvailable"
                else "getAvailabilityStatus"
            }.hook {
                if (SDK >= A13) replaceToFalse() else replaceTo(3)
            }
        }
    }
}
