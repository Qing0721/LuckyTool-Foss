package com.fosstool.app.hook.scope.android

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.A12
import com.fosstool.app.utils.A14
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK

object AllowUntrustedTouch : YukiBaseHooker() {
    override fun onHook() {
        if (SDK < A12) return
        val isEnable = prefs(ModulePrefs).getBoolean("allow_untrusted_touch", false)

        "com.android.server.input.UntrustedTouchController".toClass().apply {
            method { name = "isOplusTrustedApp" }.hook {
                if (isEnable) replaceToTrue()
            }
            method { name = "showTipsDialog" }.hook {
                if (isEnable) intercept()
            }
        }
        "com.android.server.wm.WindowStateExtImpl".toClass().apply {
            method { name = "isOplusTrustedWindow" }.hook {
                if (isEnable) replaceToTrue()
            }
        }
        if (SDK >= A14) return
        "android.hardware.input.InputManager".toClass().apply {
            method { name = "getBlockUntrustedTouchesMode" }.hook {
                if (isEnable) replaceTo(0)
            }
        }
    }
}
