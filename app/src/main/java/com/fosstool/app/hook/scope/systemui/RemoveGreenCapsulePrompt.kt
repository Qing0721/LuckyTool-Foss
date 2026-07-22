package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.A14
import com.fosstool.app.utils.SDK

object RemoveGreenCapsulePrompt : YukiBaseHooker() {
    override fun onHook() {
        VariousClass(
            "com.oplusos.systemui.statusbar.widget.SystemPromptView",
            "com.oplus.systemui.statusbar.widget.SystemPromptView"
        ).toClass().apply {
            method { name = "updateViewVisible" }.hook {
                before {
                    field { name = "disable" }.get(instance).setTrue()
                }
            }
            method {
                name = if (SDK >= A14) "disable"
                else "setViewVisibleByDisable"
            }.hook {
                before { args().first().setTrue() }
            }
        }
    }
}
