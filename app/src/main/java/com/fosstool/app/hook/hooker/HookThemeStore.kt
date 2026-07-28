package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.themestore.UnlockThemeStoreVip
import com.fosstool.app.utils.ModulePrefs

class HookThemeStore : YukiBaseHooker() {
    override fun onHook() {
        if (prefs(ModulePrefs).getBoolean("unlock_themestore_vip",false)) {
            loadHooker(UnlockThemeStoreVip)
        }
    }
}
