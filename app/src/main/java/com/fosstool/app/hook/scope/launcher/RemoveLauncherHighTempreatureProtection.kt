package com.fosstool.app.hook.scope.launcher

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method

object RemoveLauncherHighTempreatureProtection : YukiBaseHooker() {
    override fun onHook() {
        "com.android.launcher.hightemperatureprotection.HighTemperatureProtectionManager".toClass()
            .apply {
                method { name = "isHighTempProtectedEnable" }.hook {
                    replaceToFalse()
                }
                method {
                    name = "isInterceptItemClickFromHighTempreatureProtection"
                    paramCount = 2
                }.hook {
                    replaceToFalse()
                }
            }
    }
}
