package com.fosstool.app.hook.scope.android

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK

object AppSplashScreen : YukiBaseHooker() {
    override fun onHook() {
        val isEnable = prefs(ModulePrefs).getBoolean("disable_splash_screen", false)
        if (SDK < A13) return

        "com.android.server.wm.StartingSurfaceController".toClass().apply {
            method { name = "showStartingWindow";paramCount = 5 }.hook {
                if (isEnable) intercept()
            }
        }
    }
}
