package com.fosstool.app.hook.scope.launcher

import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getOSVersionCode
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.type.java.IntType

object CustomMultiWindowDisplayUpperLimit : YukiBaseHooker() {
    override fun onHook() {
        if (getOSVersionCode < 33) return

        var forceEnable = prefs(ModulePrefs).getBoolean("force_enable_multi_window_mode", false)
        dataChannel.wait<Boolean>("force_enable_multi_window_mode") { forceEnable = it }
        var limit = prefs(ModulePrefs).getInt("custom_multi_window_display_upper_limit", 2)
        dataChannel.wait<Int>("custom_multi_window_display_upper_limit") { limit = it }

        val clazz = "com.android.server.wm.FlexibleWindowManagerService".toClassOrNull(appClassLoader)
        if (clazz == null) {
            YLog.error("CustomMultiWindowDisplayUpperLimit: FlexibleWindowManagerService not found")
            return
        }
        clazz.method { name = "getMaxWinNum"; returnType = IntType }
            .ignored()
            .hook {
                after {
                    if (forceEnable && limit > 0) result = limit
                }
            }
    }
}
