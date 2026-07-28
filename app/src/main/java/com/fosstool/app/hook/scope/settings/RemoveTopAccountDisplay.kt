package com.fosstool.app.hook.scope.settings

import com.fosstool.app.utils.A13
import com.fosstool.app.utils.SDK
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object RemoveTopAccountDisplay : YukiBaseHooker() {
    override fun onHook() {
        val clazz = "com.oplus.settings.feature.homepage.user.UserPreferenceController"
            .toClassOrNull(appClassLoader) ?: return
        if (SDK >= A13) {
            clazz.method { name = "checkAvailable" }.ignored().hook { replaceToFalse() }
        } else {
            clazz.method { name = "getAvailabilityStatus" }.ignored().hook { replaceTo(3) }
        }
    }
}
