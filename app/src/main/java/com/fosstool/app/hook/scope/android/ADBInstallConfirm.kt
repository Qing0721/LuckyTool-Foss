package com.fosstool.app.hook.scope.android

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.ModulePrefs

object ADBInstallConfirm : YukiBaseHooker() {
    override fun onHook() {
        val isEnable = prefs(ModulePrefs).getBoolean("remove_adb_install_confirm", false)

        VariousClass(
            "com.android.server.pm.ColorPackageInstallInterceptManager",
            "com.android.server.pm.OplusPackageInstallInterceptManager"
        ).toClass().apply {
            method { name = "allowInterceptAdbInstallInInstallStage" }.hook {
                if (isEnable) replaceToFalse()
            }
        }
    }
}
