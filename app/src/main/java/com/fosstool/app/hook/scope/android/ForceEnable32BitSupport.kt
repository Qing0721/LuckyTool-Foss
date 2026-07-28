package com.fosstool.app.hook.scope.android

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK

object ForceEnable32BitSupport : YukiBaseHooker() {
    override fun onHook() {
        if (SDK < A13) return
        if (!prefs(ModulePrefs).getBoolean("force_enable_32_bit_support", false)) return

        val cls = "com.android.server.pm.OplusPackageManagerHelper".toClassOrNull(appClassLoader)
        if (cls == null) {
            YLog.error("ForceEnable32BitSupport: OplusPackageManagerHelper not found")
            return
        }
        cls.method { name = "allowInstall32BitApp" }.ignored().hook { replaceToTrue() }
    }
}
