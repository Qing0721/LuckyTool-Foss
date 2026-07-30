package com.fosstool.app.hook.scope.android

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.type.java.ListClass
import com.fosstool.app.utils.ModulePrefs
import de.robv.android.xposed.XposedHelpers

object SetAppUpdateDotDisplayMode : YukiBaseHooker() {
    override fun onHook() {
        val mode = prefs(ModulePrefs).getString("set_app_update_dot_display_mode", "0") ?: "0"
        if (mode == "0") return

        val ext = "com.android.server.pm.PackageManagerServiceExtImpl".toClassOrNull(appClassLoader)
        if (ext == null) {
            YLog.error("SetAppUpdateDotDisplayMode: PackageManagerServiceExtImpl not found")
            return
        }
        val helper = "com.android.server.pm.OplusOsPackageManagerHelper".toClassOrNull(appClassLoader)
        if (helper == null) {
            YLog.error("SetAppUpdateDotDisplayMode: OplusOsPackageManagerHelper not found")
        }

        ext.method { name = "handleSuccessAtEndInHPPI"; paramCount = 6 }.ignored().hook {
            after {
                if (mode != "1") return@after
                val pkgName = (args(2).any() as? String).orEmpty()
                if (pkgName.isEmpty()) return@after
                val installerRaw = args(3).any()
                val isUpdate = args(4).any() as? Boolean ?: false
                val installer = when (installerRaw) {
                    is String -> installerRaw
                    null -> ""
                    else -> runCatching {
                        XposedHelpers.getObjectField(installerRaw, "mInstallerPackageName") as? String
                    }.getOrNull().orEmpty()
                }

                @Suppress("UNCHECKED_CAST")
                val marketList: List<Any?> = runCatching {
                    ext.field { name = "DEFAULT_MARKET_LIST"; type = ListClass }
                        .ignored().get().any() as? List<Any?>
                }.getOrNull() ?: emptyList()

                if (!isUpdate && !marketList.contains(installer)) return@after
                val helperCls = helper ?: return@after
                runCatching {
                    XposedHelpers.callStaticMethod(helperCls, "addPkgToNotLaunchedList", pkgName)
                }
            }
        }
    }
}
