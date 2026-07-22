package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method

object RemoveAodMusicWhitelist : YukiBaseHooker() {
    override fun onHook() {
        "com.oplusos.systemui.aod.mediapanel.AodMediaDataListener\$Companion".toClass().apply {
            method { name = "isAodMediaSupport" }.hook {
                replaceToTrue()
            }
            method { name = "isAodMediaSupportWithoutFeature" }.hook {
                replaceToTrue()
            }
        }
    }
}
