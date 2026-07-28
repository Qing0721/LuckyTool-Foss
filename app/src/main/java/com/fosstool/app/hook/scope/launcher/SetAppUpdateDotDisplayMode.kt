package com.fosstool.app.hook.scope.launcher

import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getOSVersionCode
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog

object SetAppUpdateDotDisplayMode : YukiBaseHooker() {
    override fun onHook() {
        if (getOSVersionCode < 33) return
        val mode = prefs(ModulePrefs).getString("set_app_update_dot_display_mode", "0") ?: "0"
        if (mode != "2") return

        val clazz = "com.android.launcher3.BubbleTextView".toClassOrNull(appClassLoader)
        if (clazz == null) {
            YLog.error("SetAppUpdateDotDisplayMode: BubbleTextView not found")
            return
        }
        clazz.method { name = "isShouldShowGreenDot" }.ignored().hook { replaceToFalse() }
    }
}
