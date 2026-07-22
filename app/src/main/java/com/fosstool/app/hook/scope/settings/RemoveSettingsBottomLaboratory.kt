package com.fosstool.app.hook.scope.settings

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method

object RemoveSettingsBottomLaboratory : YukiBaseHooker() {
    override fun onHook() {
        "com.oplus.settings.feature.homepage.TopLevelLaboratoryPreferenceController".toClass()
            .apply {
                method { name = "getAvailabilityStatus" }.hook {
                    replaceTo(3)
                }
            }
    }
}
