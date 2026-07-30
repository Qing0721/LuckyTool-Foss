package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog

object RemoveGreenDotPrivacyPrompt : YukiBaseHooker() {
    override fun onHook() {
        var hit = false

        VariousClass(
            "com.oplusos.systemui.statusbar.events.ViewState",
            "com.oplus.systemui.privacy.ViewState"
        ).toClassOrNull(appClassLoader)?.let {
            hit = true
            it.method { name = "shouldShowDot" }.ignored().hook { replaceToFalse() }
        }

        "com.android.systemui.statusbar.events.ViewState".toClassOrNull(appClassLoader)?.let {
            hit = true
            it.method { name = "shouldShowDot" }.ignored().hook { replaceToFalse() }
        }

        if (!hit) YLog.error("RemoveGreenDotPrivacyPrompt: no ViewState class found")
    }
}
