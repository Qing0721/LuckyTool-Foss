package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.A14
import com.fosstool.app.utils.SDK

object RemoveGreenDotPrivacyPrompt : YukiBaseHooker() {
    override fun onHook() {
        "com.android.systemui.statusbar.events.PrivacyDotViewController".toClass().apply {
            hookMethod()
        }
        "com.oplusos.systemui.statusbar.events.OplusPrivacyDotViewController".toClassOrNull()
            ?.hookMethod()

        if (SDK < A14) return
        "com.oplus.systemui.privacy.OplusPrivacyDotViewController".toClass().apply {
            hookMethod()
        }
    }

    private fun Class<*>.hookMethod() {
        method { name = "showDotView";paramCount = 2 }.hook {
            intercept()
        }
        method { name = "updateDesignatedCorner";paramCount = 2 }.hook {
            intercept()
        }
    }
}
