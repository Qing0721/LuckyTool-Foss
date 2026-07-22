package com.fosstool.app.hook.scope.settings

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method

object ForceDisplayAutoLaunchJumpOption : YukiBaseHooker() {
    override fun onHook() {
        "com.oplus.settings.feature.appmanager.controller.AutoLaunchMgrPreferenceController"
            .toClass()
            .method { name = "getAvailabilityStatus" }
            .hook {
                replaceTo(0)
            }
    }
}
