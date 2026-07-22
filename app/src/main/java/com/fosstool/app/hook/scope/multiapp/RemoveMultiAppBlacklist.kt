package com.fosstool.app.hook.scope.multiapp

import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getOSVersionCode
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method

object RemoveMultiAppBlacklist : YukiBaseHooker() {
    override fun onHook() {
        if (getOSVersionCode < 31) return
        if (!prefs(ModulePrefs).getBoolean("remove_multi_app_blacklist", false)) return
        "com.oplus.multiapp.utils.MultiAppBlackListUpdateHelper".toClass().apply {
            method { name = "loadMultiappBlackListConfig" }.hook {
                before { intercept() }
            }
        }
    }
}
