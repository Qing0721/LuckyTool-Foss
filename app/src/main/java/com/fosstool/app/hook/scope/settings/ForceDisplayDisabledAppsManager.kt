package com.fosstool.app.hook.scope.settings

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object ForceDisplayDisabledAppsManager : YukiBaseHooker() {
    override fun onHook() {
        "com.android.settings.applications.disableapps.DisabledAppsPreferenceController"
            .toClassOrNull(appClassLoader)
            ?.method { name = "getAvailabilityStatus" }
            ?.ignored()
            ?.hook { replaceTo(0) }
    }
}
