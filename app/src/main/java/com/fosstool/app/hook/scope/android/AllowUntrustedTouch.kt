package com.fosstool.app.hook.scope.android

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.A14
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK

object AllowUntrustedTouch : YukiBaseHooker() {
    override fun onHook() {

        val isEnable = prefs(ModulePrefs).getBoolean("allow_untrusted_touch", false)

        runCatching {
            "com.android.server.input.UntrustedTouchController".toClass().apply {
                method { name = "isOplusTrustedApp" }.ignored().hook {
                    if (isEnable) replaceToTrue()
                }
                method { name = "showTipsDialog" }.ignored().hook {
                    if (isEnable) intercept()
                }
            }
        }
        runCatching {
            "com.android.server.wm.WindowStateExtImpl".toClass().apply {
                method { name = "isOplusTrustedWindow" }.ignored().hook {
                    if (isEnable) replaceToTrue()
                }
            }
        }
        if (SDK >= A14) return
        runCatching {
            "android.hardware.input.InputManager".toClass().apply {
                method { name = "getBlockUntrustedTouchesMode" }.ignored().hook {
                    if (isEnable) replaceTo(0)
                }
            }
        }
    }
}
