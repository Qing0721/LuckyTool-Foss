package com.fosstool.app.hook.scope.settings

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object RemoveSettingsBottomLaboratory : YukiBaseHooker() {
    override fun onHook() {
        "com.oplus.settings.feature.homepage.TopLevelLaboratoryPreferenceController"
            .toClassOrNull(appClassLoader)
            ?.method { name = "getAvailabilityStatus" }
            ?.ignored()
            ?.hook { replaceTo(3) }
    }
}
