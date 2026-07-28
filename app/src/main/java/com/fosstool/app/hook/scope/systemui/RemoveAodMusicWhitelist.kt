package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object RemoveAodMusicWhitelist : YukiBaseHooker() {
    override fun onHook() {
        "com.oplusos.systemui.aod.mediapanel.AodMediaDataListener\$Companion"
            .toClassOrNull(appClassLoader)?.let { c ->
                c.method { name = "isAodMediaSupport" }.ignored().hook { replaceToTrue() }
                c.method { name = "isAodMediaSupportWithoutFeature" }.ignored().hook { replaceToTrue() }
            }
    }
}
