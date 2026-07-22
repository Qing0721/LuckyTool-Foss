package com.fosstool.app.hook.scope.gesture

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK

object RemoveBackGestureConfirmationLimit : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("remove_back_gesture_confirmation_limit", false)) return
        if (SDK !in 35..36) return
        runCatching {
            "com.oplus.systemui.navigationbar.gesture.sidegesture.SideGestureDetector".toClass().apply {
                method {
                    name = "shouldRespondToGesture"
                    returnType = BooleanType
                }.hook { replaceToTrue() }
                method {
                    name = "shouldInjectToGestureMode"
                    returnType = BooleanType
                }.hook { replaceToTrue() }
            }
        }
    }
}
