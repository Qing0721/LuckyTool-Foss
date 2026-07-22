package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.safecenter.UnlockStartupLimitOld
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK

object HookSafeCenter : YukiBaseHooker() {
    override fun onHook() {
        if (SDK < A13 && prefs(ModulePrefs).getBoolean("unlock_startup_limit", false)) {
            loadHooker(UnlockStartupLimitOld)
        }
    }
}
