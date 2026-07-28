package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.aod.AodRandomTextAndTypeface
import com.fosstool.app.utils.getOSVersionCode

object HookAod : YukiBaseHooker() {
    override fun onHook() {

        if (getOSVersionCode >= 26) loadHooker(AodRandomTextAndTypeface)
    }
}
