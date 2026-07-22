package com.fosstool.app.hook.scope.settings

import com.fosstool.app.utils.ModulePrefs
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.StringClass

object AppSpecificMediaVolume : YukiBaseHooker() {
    override fun onHook() {
        "com.android.settings.SettingsPreferenceFragment".toClass().apply {
            method {
                name = "removePreference"
                param(StringClass)
            }.hook {
                before {
                    if (args().first().string() == "voice_mode_category") intercept()
                }
            }
        }
    }
}
