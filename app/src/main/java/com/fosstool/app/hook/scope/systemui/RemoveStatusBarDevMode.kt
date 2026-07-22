package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method

object RemoveStatusBarDevMode : YukiBaseHooker() {
    override fun onHook() {
        VariousClass(
            "com.coloros.systemui.statusbar.policy.ColorSystemPromptController",
            "com.oplusos.systemui.statusbar.policy.SystemPromptController",
            "com.oplus.systemui.statusbar.controller.SystemPromptController"
        ).toClass().apply {
            method { name = "updateDeveloperMode" }.hook {
                intercept()
            }
        }
    }
}
