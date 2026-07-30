package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.gesture.CustomAonGestureScrollPageWhitelist
import com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker
import com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker.Mode

object HookGesture : YukiBaseHooker() {
    override fun onHook() {

        loadHooker(SystemPropertiesOverrideEngineHooker(mode = Mode.RM0_Q))

        loadHooker(CustomAonGestureScrollPageWhitelist)
    }
}
