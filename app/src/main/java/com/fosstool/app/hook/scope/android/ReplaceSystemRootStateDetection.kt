package com.fosstool.app.hook.scope.android

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.utils.A12
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK
import com.fosstool.app.utils.getOSVersionCode

object ReplaceSystemRootStateDetection : YukiBaseHooker() {
    override fun onHook() {
        if (SDK < A12) return
        if (!prefs(ModulePrefs).getBoolean("replace_system_root_state_detection", false)) return

        if (getOSVersionCode > 26) {
            "com.android.server.oplus.heimdall.HeimdallService".toClassOrNull(appClassLoader)?.apply {
                method { name = "isRootEnable" }.ignored().hook { replaceToFalse() }
            }
        }
        "com.android.server.oplus.heimdall.service.RootService".toClassOrNull(appClassLoader)?.apply {
            method { name = "isRoot" }.ignored().hook { replaceToFalse() }
        }
        "com.android.server.oplus.heimdall.root.RootDetector".toClassOrNull(appClassLoader)?.apply {
            method { name = "checkDeviceRootStatus" }.ignored().hook { intercept() }
        }
    }
}
