package com.fosstool.app.hook.scope.android

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.utils.ModulePrefs

object ADBInstallConfirm : YukiBaseHooker() {
    override fun onHook() {
        val isEnable = prefs(ModulePrefs).getBoolean("remove_adb_install_confirm", false)
        if (!isEnable) return

        VariousClass(
            "com.android.server.pm.ColorPackageInstallInterceptManager",
            "com.android.server.pm.OplusPackageInstallInterceptManager",
        ).toClassOrNull(appClassLoader)?.apply {
            method { name = "allowInterceptAdbInstallInInstallStage" }.ignored().hook { replaceToFalse() }
        }
    }
}
