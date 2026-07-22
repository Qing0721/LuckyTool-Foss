package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.gesture.CustomAonGestureScrollPageWhitelist
import com.fosstool.app.hook.scope.gesture.EnableAonGestures
import com.fosstool.app.hook.scope.gesture.EnableVolumeKeyControlFlashlight
import com.fosstool.app.hook.scope.gesture.RemoveBackGestureConfirmationLimit
import com.fosstool.app.utils.ModulePrefs

object HookGesture : YukiBaseHooker() {
    override fun onHook() {
        if (prefs(ModulePrefs).getBoolean("force_enable_aon_gestures", false)) {
            loadHooker(EnableAonGestures)
        }

        loadHooker(CustomAonGestureScrollPageWhitelist)

        if (prefs(ModulePrefs).getBoolean("enable_volume_key_control_flashlight", false)) {
            loadHooker(EnableVolumeKeyControlFlashlight)
        }

        if (prefs(ModulePrefs).getBoolean("remove_back_gesture_confirmation_limit", false)) {
            loadHooker(RemoveBackGestureConfirmationLimit)
        }
    }
}
