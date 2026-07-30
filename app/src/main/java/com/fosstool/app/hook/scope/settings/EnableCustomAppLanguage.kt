package com.fosstool.app.hook.scope.settings

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object EnableCustomAppLanguage : YukiBaseHooker() {
    override fun onHook() {
        "com.android.settings.applications.AppLocaleUtil".toClassOrNull(appClassLoader)
            ?.method { name = "canDisplayLocaleUi" }
            ?.ignored()
            ?.hook { replaceToTrue() }

        "com.android.settings.applications.appinfo.AppLocalePreferenceController".toClassOrNull(appClassLoader)
            ?.method { name = "getAvailabilityStatus" }
            ?.ignored()
            ?.hook { replaceTo(0) }
    }
}
