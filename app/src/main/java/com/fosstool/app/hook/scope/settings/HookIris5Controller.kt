package com.fosstool.app.hook.scope.settings

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object HookIris5Controller : YukiBaseHooker() {
    override fun onHook() {
        val controllers = listOf(
            "com.oplus.settings.feature.display.controller.Iris5MotionFluencySwitchController",
            "com.oplus.settings.feature.display.controller.Iris5MotionFluencyController",
            "com.oplus.settings.feature.display.controller.Iris5VideoDisplayEnhancementController",
            "com.oplus.settings.feature.display.controller.Iris5VideoSuperResolutionController",
        )
        for (cn in controllers) {
            cn.toClassOrNull(appClassLoader)?.apply {
                method { name = "is2kReject" }.ignored().hook { replaceToFalse() }
                method { name = "isSupport120With2K" }.ignored().hook { replaceToTrue() }
            }
        }
    }
}
