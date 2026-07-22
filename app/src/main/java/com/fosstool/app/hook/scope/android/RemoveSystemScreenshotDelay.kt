package com.fosstool.app.hook.scope.android

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.LongType
import com.fosstool.app.utils.ModulePrefs

object RemoveSystemScreenshotDelay : YukiBaseHooker() {
    override fun onHook() {
        val isEnable = prefs(ModulePrefs).getBoolean("remove_system_screenshot_delay", false)

        "com.android.server.policy.PhoneWindowManager".toClass().apply {
            method { name = "getScreenshotChordLongPressDelay";returnType = LongType }.hook {
                if (isEnable) replaceTo(0L)
            }
        }
    }
}
