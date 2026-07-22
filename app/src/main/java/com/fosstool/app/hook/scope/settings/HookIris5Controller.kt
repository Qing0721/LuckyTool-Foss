package com.fosstool.app.hook.scope.settings

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method

object HookIris5Controller : YukiBaseHooker() {
    override fun onHook() {
        val controllers = listOf(
            "com.oplus.settings.feature.display.controller.Iris5MotionFluencySwitchController",
            "com.oplus.settings.feature.display.controller.Iris5MotionFluencyController",
            "com.oplus.settings.feature.display.controller.Iris5VideoDisplayEnhancementController",
            "com.oplus.settings.feature.display.controller.Iris5VideoSuperResolutionController",
        )
        for (cn in controllers) {
            runCatching {
                cn.toClass().apply {
                    method { name = "is2kReject" }.hook {
                        replaceToFalse()
                    }
                    method { name = "isSupport120With2K" }.hook {
                        replaceToTrue()
                    }
                }
            }
        }
    }
}
