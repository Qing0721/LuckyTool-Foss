package com.fosstool.app.hook.scope.gesture

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method

object EnableVolumeKeyControlFlashlight : YukiBaseHooker() {
    override fun onHook() {
        "com.oplus.gesture.util.GestureUtil".toClass().apply {
            method { name = "hasQuickOperateTorchFeature" }.hook {
                replaceToTrue()
            }
        }
    }
}
