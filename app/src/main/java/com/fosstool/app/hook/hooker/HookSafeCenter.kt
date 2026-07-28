package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.safecenter.UnlockStartupLimitOld
import com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker
import com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker.Mode
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK

class HookSafeCenter : YukiBaseHooker() {
    override fun onHook() {
        loadHooker(SystemPropertiesOverrideEngineHooker(mode = Mode.RM0_Q))

        if (SDK < A13 && prefs(ModulePrefs).getBoolean("unlock_startup_limit", false)) {
            loadHooker(UnlockStartupLimitOld)
        }
    }
}
