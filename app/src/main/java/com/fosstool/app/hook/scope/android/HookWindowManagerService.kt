package com.fosstool.app.hook.scope.android

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.ModulePrefs

object HookWindowManagerService : YukiBaseHooker() {
    override fun onHook() {
        val isDpi = prefs(ModulePrefs).getBoolean("remove_dpi_restart_recovery", false)

        "com.android.server.wm.OplusWindowManagerService".toClass().apply {
            method {
                name = "clearForcedDisplayDensityForUser"
                paramCount = 2;superClass()
            }.hook { if (isDpi) intercept() }
        }
    }
}
