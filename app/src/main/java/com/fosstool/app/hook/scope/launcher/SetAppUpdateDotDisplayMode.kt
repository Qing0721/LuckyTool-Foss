package com.fosstool.app.hook.scope.launcher

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK

object SetAppUpdateDotDisplayMode : YukiBaseHooker() {
    override fun onHook() {
        if (SDK < A13) return
        val mode = prefs(ModulePrefs).getString("set_app_update_dot_display_mode", "0") ?: "0"
        if (mode != "2") return

        runCatching {
            "com.android.launcher3.BubbleTextView".toClass().apply {
                method {
                    name = "isShouldShowGreenDot"
                    emptyParam()
                    returnType = BooleanType
                }.hook {
                    replaceToFalse()
                }
            }
        }
    }
}
