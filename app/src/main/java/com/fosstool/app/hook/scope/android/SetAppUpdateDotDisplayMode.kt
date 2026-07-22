package com.fosstool.app.hook.scope.android

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK

object SetAppUpdateDotDisplayMode : YukiBaseHooker() {
    override fun onHook() {
        if (SDK < A13) return
        val mode = prefs(ModulePrefs).getString("set_app_update_dot_display_mode", "0") ?: "0"
        if (mode != "1") return

        runCatching {
            val marketList = runCatching {
                "com.android.server.pm.OplusOsPackageManagerHelper".toClass()
                    .field { name = "DEFAULT_MARKET_LIST" }.get().any() as? List<*>
            }.getOrNull().orEmpty()

            "com.android.server.pm.PackageManagerServiceExtImpl".toClass().apply {
                method {
                    name = "handleSuccessAtEndInHPPI"
                    paramCount = 6
                }.hook {
                    after {
                        val pkgName = args(2).string().ifEmpty { return@after }
                        val installerRaw = args(3).any()
                        val isUpdate = args(4).boolean()
                        val installer = when (installerRaw) {
                            is String -> installerRaw
                            null -> ""
                            else -> runCatching {
                                installerRaw.current().field { name = "mInstallerPackageName" }
                                    .string()
                            }.getOrNull().orEmpty()
                        }
                        if (!isUpdate && !marketList.contains(installer)) return@after
                        runCatching {
                            "com.android.server.pm.OplusOsPackageManagerHelper".toClass()
                                .method {
                                    name = "addPkgToNotLaunchedList"
                                    param(StringClass)
                                }.get().call(pkgName)
                        }
                    }
                }
            }
        }
    }
}
