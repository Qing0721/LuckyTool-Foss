package com.fosstool.app.hook.scope.packageinstaller

import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.fosstool.app.utils.ModulePrefs
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.java.IntType

object DisableStartAppDetail : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("disable_start_app_detail", false)) return

        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findMethod {
                matcher {
                    returnType(IntType.name)
                    usingStrings("count_canceled_by_app_detail", "com.oplus.appdetail")
                }
            }.apply {
                checkDataList("DisableStartAppDetail")
                val target = firstOrNullSafe() ?: return@apply
                target.className.toClassOrNull(appClassLoader)
                    ?.method { name = target.methodName; returnType = IntType }
                    ?.ignored()
                    ?.hook { replaceTo(9) }
            }
        }
    }
}
