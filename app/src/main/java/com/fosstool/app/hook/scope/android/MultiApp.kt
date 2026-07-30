package com.fosstool.app.hook.scope.android

import android.util.ArraySet
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getOSVersionCode

object MultiApp : YukiBaseHooker() {
    override fun onHook() {
        var supportMode = prefs(ModulePrefs).getString("set_multi_app_support_mode", "0") ?: "0"
        dataChannel.wait<String>("set_multi_app_support_mode") { supportMode = it }
        val isEnable = supportMode == "1"
        var enabledMulti = prefs(ModulePrefs).getStringSet("multi_app_custom_list", ArraySet())
        dataChannel.wait<Set<String>>("multi_app_custom_list") { enabledMulti = it }

        val removeLimit = prefs(ModulePrefs).getBoolean("remove_multi_app_created_num_limit_for_users", false) ||
            prefs(ModulePrefs).getBoolean("remove_multi_app_created_num_limit_for_apps", false)

        val cfg = "com.oplus.multiapp.OplusMultiAppConfig".toClassOrNull(appClassLoader) ?: return
        cfg.method { name = "getAllowedPkgList" }.ignored().hook {
            before {
                if (!isEnable || enabledMulti.isEmpty()) return@before
                result = java.util.ArrayList(enabledMulti)
            }
        }
        if (getOSVersionCode >= 31) {
            cfg.method { name = "getMaxCreatedNum" }.ignored().hook {
                before { if (removeLimit) result = 1000 }
            }
        }
        if (getOSVersionCode >= 38) {
            cfg.method { name = "getMaxCloneUserNum" }.ignored().hook {
                before { if (removeLimit) result = 10 }
            }
        }
    }
}
