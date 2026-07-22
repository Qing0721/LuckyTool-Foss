package com.fosstool.app.hook.scope.settings

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method

object EnableCustomAppLanguage : YukiBaseHooker() {
    override fun onHook() {
        "com.android.settings.applications.AppLocaleUtil".toClass().apply {
            method { name = "canDisplayLocaleUi" }.hook {
                replaceToTrue()
            }
        }
        "com.android.settings.applications.appinfo.AppLocalePreferenceController".toClass().apply {
            method { name = "getAvailabilityStatus" }.hook {
                replaceTo(0)
            }
        }
    }
}
