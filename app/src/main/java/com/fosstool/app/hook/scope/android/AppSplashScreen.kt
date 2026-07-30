package com.fosstool.app.hook.scope.android

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.ModulePrefs

object AppSplashScreen : YukiBaseHooker() {
    override fun onHook() {

        val isEnable = prefs(ModulePrefs).getBoolean("disable_splash_screen", false)

        runCatching {
            "com.android.server.wm.StartingSurfaceController".toClass().apply {
                method { name = "showStartingWindow";paramCount = 5 }.ignored().hook {
                    if (isEnable) intercept()
                }
            }
        }
    }
}
