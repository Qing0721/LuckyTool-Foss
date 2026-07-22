package com.fosstool.app.hook.scope.launcher

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.BooleanType

object EnableAppUpdateDot : YukiBaseHooker() {
    override fun onHook() {
        runCatching {
            "com.android.launcher.settings.LauncherSettingsUtils".toClass().apply {
                method { name = "isSupportAppUpdateDot"; returnType = BooleanType }.hook {
                    replaceToTrue()
                }
            }
        }
        runCatching {
            "com.android.launcher.settings.LauncherSettingsUtils".toClass().apply {
                method { name = "isSupportAppUpdateDotSwitch"; returnType = BooleanType }.hook {
                    replaceToTrue()
                }
            }
        }
    }
}
