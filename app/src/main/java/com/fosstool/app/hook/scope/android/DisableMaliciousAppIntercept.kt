package com.fosstool.app.hook.scope.android

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getOSVersionCode

object DisableMaliciousAppIntercept : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("disable_malicious_app_intercept", false)) return
        if (getOSVersionCode < 38) return
        "com.android.server.wm.OplusAppStartConfirmManager".toClassOrNull(appClassLoader)?.apply {
            method { name = "checkMaliciousIntercept" }.ignored().hook { replaceToFalse() }
        }
    }
}
