package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.directui.RemoveTouchAppRecommendCard
import com.fosstool.app.utils.ModulePrefs

object HookDirectUI : YukiBaseHooker() {
    override fun onHook() {
        if (prefs(ModulePrefs).getBoolean("remove_touch_app_recommend_card", false)) {
            loadHooker(RemoveTouchAppRecommendCard)
        }
    }
}
