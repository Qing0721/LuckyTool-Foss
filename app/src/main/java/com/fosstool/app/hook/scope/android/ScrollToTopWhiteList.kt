package com.fosstool.app.hook.scope.android

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.utils.ModulePrefs

object ScrollToTopWhiteList : YukiBaseHooker() {
    override fun onHook() {

        val mode = prefs(ModulePrefs).getString("set_click_statusbar_scroll_to_top_mode", "0")
        if (mode == "0") return

        val cls = "com.android.server.OplusScrollToTopRusHelper".toClassOrNull(appClassLoader) ?: return
        cls.method { name = "isInWhiteList"; paramCount = 1 }.ignored().hook {
            before {
                when (mode) {
                    "1" -> result = false
                    "2" -> result = true
                }
            }
        }
    }
}
