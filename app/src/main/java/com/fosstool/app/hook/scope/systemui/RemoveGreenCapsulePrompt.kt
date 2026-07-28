package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog

object RemoveGreenCapsulePrompt : YukiBaseHooker() {
    override fun onHook() {
        val clazz = VariousClass(
            "com.oplusos.systemui.statusbar.policy.SystemPromptController",
            "com.oplus.systemui.statusbar.controller.SystemPromptController"
        ).toClassOrNull(appClassLoader)
        if (clazz == null) {
            YLog.error("RemoveGreenCapsulePrompt: SystemPromptController not found")
            return
        }
        clazz.method { name = "updatePromptIcon" }.ignored().hook { intercept() }
    }
}
