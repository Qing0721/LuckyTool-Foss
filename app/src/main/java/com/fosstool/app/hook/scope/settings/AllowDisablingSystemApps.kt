package com.fosstool.app.hook.scope.settings

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object AllowDisablingSystemApps : YukiBaseHooker() {
    override fun onHook() {
        "com.oplus.settings.adaptor.AppButtonsPreferenceControllerAdaptor".toClassOrNull(appClassLoader)
            ?.method { name = "setUninstallButtonEnabled" }
            ?.ignored()
            ?.hook {
                before {
                    if (args.isNotEmpty()) args[0] = true
                }
            }
    }
}
