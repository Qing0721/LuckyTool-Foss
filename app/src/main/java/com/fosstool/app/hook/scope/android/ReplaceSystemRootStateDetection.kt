package com.fosstool.app.hook.scope.android

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.fosstool.app.utils.A12
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK
import com.fosstool.app.utils.getOSVersionCode

object ReplaceSystemRootStateDetection : YukiBaseHooker() {
    override fun onHook() {
        if (SDK < A12) return
        if (!prefs(ModulePrefs).getBoolean("replace_system_root_state_detection", false)) return

        if (getOSVersionCode > 26) {
            runCatching {
                "com.android.server.oplus.heimdall.HeimdallService".toClass().apply {
                    method { name = "isRootEnable"; returnType = BooleanType }.hook {
                        replaceToFalse()
                    }
                }
            }
        }
        runCatching {
            "com.android.server.oplus.heimdall.service.RootService".toClass().apply {
                method { name = "isRoot"; returnType = BooleanType }.hook {
                    replaceToFalse()
                }
            }
        }
        runCatching {
            "com.android.server.oplus.heimdall.root.RootDetector".toClass().apply {
                method { name = "checkDeviceRootStatus" }.hook {
                    intercept()
                }
            }
        }
    }
}
