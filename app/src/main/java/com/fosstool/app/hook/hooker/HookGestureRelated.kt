package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.systemui.FullScreenGestureSideSlideBar
import com.fosstool.app.hook.scope.systemui.RemoveRotateScreenButton
import com.fosstool.app.utils.ModulePrefs

object HookGestureRelated : YukiBaseHooker() {
    override fun onHook() {
        loadHooker(FullScreenGestureSideSlideBar)

        if (prefs(ModulePrefs).getBoolean("remove_rotate_screen_button", false)) {
            loadHooker(RemoveRotateScreenButton)
        }
    }
}
