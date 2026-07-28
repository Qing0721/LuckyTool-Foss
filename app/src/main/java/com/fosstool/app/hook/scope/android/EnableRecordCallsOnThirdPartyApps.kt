package com.fosstool.app.hook.scope.android

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getOSVersionCode

object EnableRecordCallsOnThirdPartyApps : YukiBaseHooker() {
    override fun onHook() {
        if (getOSVersionCode != 30) return

        val enable = prefs(ModulePrefs).getBoolean("enable_record_calls_on_third_party_apps", false)
        val cls = "android.media.projection.MediaProjectionManagerServiceExtImpl"
            .toClassOrNull(appClassLoader) ?: return

        cls.method { name = "isOplusApp"; paramCount = 1 }.ignored().hook {
            after {
                if (!enable) return@after
                if (args(0).any() as? String == "com.oplus.audiomonitor") resultTrue()
            }
        }
    }
}
