package com.fosstool.app.hook.scope.settings

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object ForceDisplayProcessManagement : YukiBaseHooker() {
    override fun onHook() {
        VariousClass(
            "com.oplus.settings.feature.othersettings.controller.RunningApplicationsPreferenceController",
            "com.oplus.settings.feature.spfunction.RunningApplicationsPreferenceController",
        ).toClassOrNull(appClassLoader)
            ?.method { name = "getAvailabilityStatus" }
            ?.ignored()
            ?.hook { replaceTo(0) }

        "com.oplus.settings.feature.appmanager.controller.RunningApplicationsNewPreferenceController"
            .toClassOrNull(appClassLoader)
            ?.method { name = "getAvailabilityStatus" }
            ?.ignored()
            ?.hook { replaceTo(0) }
    }
}
