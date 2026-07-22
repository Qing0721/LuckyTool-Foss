package com.fosstool.app.hook.scope.settings

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method

object ForceDisplayDCBackLightMode : YukiBaseHooker() {
    override fun onHook() {

        "com.oplus.settings.feature.display.controller.DcBackLightModePreferenceController".toClass()
            .apply {
                method { name = "getAvailabilityStatus" }.hook {
                    replaceTo(0)
                }
            }
    }
}
