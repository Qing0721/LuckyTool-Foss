package com.fosstool.app.hook.scope.otherapp

import android.app.Activity
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.ModulePrefs
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object HookAlphaBackupPro : YukiBaseHooker() {
    override fun onHook() {
        val isPro = prefs(ModulePrefs).getBoolean("remove_check_license", false)
        if (!isPro) return
        "com.ruet_cse_1503050.ragib.appbackup.pro.activities.HomeActivity".toClassOrNull(appClassLoader)?.apply {
            method { name = "onCreate" }.hook {
                before {
                    instance<Activity>().intent.putExtra("licenseState", "valid_licence")
                }
            }
        }
    }
}
