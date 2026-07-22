package com.fosstool.app.hook.scope.settings

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method

object AllowDisablingSystemApps : YukiBaseHooker() {
    override fun onHook() {
        "com.oplus.settings.adaptor.AppButtonsPreferenceControllerAdaptor".toClass().apply {
            method { name = "setUninstallButtonEnabled" }.hook {
                before { args().first().setTrue() }
            }
        }
    }
}
