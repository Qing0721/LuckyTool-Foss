package com.fosstool.app.hook.scope.settings

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method

object ForceDisplayProcessManagement : YukiBaseHooker() {
    override fun onHook() {
        runCatching {
            "com.oplus.settings.feature.othersettings.controller.RunningApplicationsPreferenceController"
                .toClass().method { name = "getAvailabilityStatus" }.hook {
                    replaceTo(0)
                }
        }.onFailure {
            runCatching {
                "com.oplus.settings.feature.spfunction.RunningApplicationsPreferenceController"
                    .toClass().method { name = "getAvailabilityStatus" }.hook {
                        replaceTo(0)
                    }
            }
        }
        "com.oplus.settings.feature.appmanager.controller.RunningApplicationsNewPreferenceController".toClass()
            .apply {
                method { name = "getAvailabilityStatus" }.hook {
                    replaceTo(0)
                }
            }
    }
}
