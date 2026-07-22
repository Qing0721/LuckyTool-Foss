package com.fosstool.app.hook.scope.android

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.hasMethod
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK

object ScrollToTopWhiteList : YukiBaseHooker() {
    override fun onHook() {
        if (SDK < A13) return
        val mode = prefs(ModulePrefs).getString("set_click_statusbar_scroll_to_top_mode", "0")
        if (mode == "0") return

        "com.android.server.OplusScrollToTopRusHelper".toClass().apply {
            if (hasMethod { name = "isInWhiteList" }) {
                method { name = "isInWhiteList";paramCount = 1 }.hook {
                    before {
                        when (mode) {
                            "1" -> resultFalse()
                            "2" -> resultTrue()
                        }
                    }
                }
            }
        }
    }
}
