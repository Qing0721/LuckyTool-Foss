package com.fosstool.app.hook.scope.launcher

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getOSVersionCode

object CustomMultiWindowDisplayUpperLimit : YukiBaseHooker() {
    override fun onHook() {
        if (getOSVersionCode < 33) return

        var forceEnable = prefs(ModulePrefs).getBoolean("force_enable_multi_window_mode", false)
        dataChannel.wait<Boolean>("force_enable_multi_window_mode") { forceEnable = it }
        var limit = prefs(ModulePrefs).getInt("custom_multi_window_display_upper_limit", 2)
        dataChannel.wait<Int>("custom_multi_window_display_upper_limit") { limit = it }

        runCatching {
            "com.android.server.wm.FlexibleWindowManagerService".toClass().apply {
                method {
                    name = "getMaxWinNum"
                    param(IntType)
                    returnType = IntType
                }.hook {
                    before {
                        if (forceEnable && limit > 0) result = limit
                    }
                }
            }
        }
    }
}
