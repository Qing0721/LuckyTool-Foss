package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.systemui.FingerPrintIconAnim

object HookFingerPrintRelated : YukiBaseHooker() {
    override fun onHook() {
        loadHooker(FingerPrintIconAnim)
    }
}
