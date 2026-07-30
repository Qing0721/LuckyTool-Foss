package com.fosstool.app.hook.scope.packageinstaller

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import de.robv.android.xposed.XposedHelpers

object AllowReplaceInstall : YukiBaseHooker() {
    override fun onHook() {
        "com.android.packageinstaller.oplus.OPlusPackageInstallerActivity".toClassOrNull(appClassLoader)
            ?.method { name = "parseReplaceInstall" }
            ?.ignored()
            ?.hook {
                before {
                    runCatching {
                        XposedHelpers.callMethod(instance, "preSafeInstall")
                    }
                    result = null
                }
            }
    }
}
