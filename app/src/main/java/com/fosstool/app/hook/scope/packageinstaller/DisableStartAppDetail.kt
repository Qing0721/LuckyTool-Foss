package com.fosstool.app.hook.scope.packageinstaller

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.ModulePrefs

object DisableStartAppDetail : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("disable_start_app_detail", false)) return

        runCatching { hookViaDexKit() }

        runCatching { hookViaHeuristic() }

        runCatching {
            "com.android.packageinstaller.oplus.common.FeatureOption".toClass().apply {
                method {
                    name {
                        it.contains("StartApp", ignoreCase = true) ||
                            it.contains("AppDetail", ignoreCase = true)
                    }
                    returnType = BooleanType
                }.hookAll {
                    after { resultFalse() }
                }
            }
        }
    }

    private fun hookViaDexKit() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findMethod {
                matcher {
                    paramTypes(IntType.name)
                    usingStrings("count_canceled_by_app_detail", "com.oplus.appdetail")
                }
            }.apply {
                checkDataList("DisableStartAppDetail", onlyOne = false)
                if (isEmpty()) return@apply
                val target = firstOrNull { it.className.contains("AppDetailRedirectionUtils") }
                    ?: first()
                target.className.toClass().apply {
                    method {
                        name = target.methodName
                        param(IntType)
                    }.hookAll {
                        intercept()
                    }
                }
            }
        }
    }

    private fun hookViaHeuristic() {
        listOf(
            "com.android.packageinstaller.oplus.InstallSuccess",
            "com.android.packageinstaller.InstallSuccess",
            "com.android.packageinstaller.oplus.ui.InstallSuccess"
        ).forEach { className ->
            runCatching {
                className.toClass().apply {
                    method {
                        name { n ->
                            n.contains("startApp", ignoreCase = true) ||
                                n.contains("launchApp", ignoreCase = true) ||
                                n.contains("openApp", ignoreCase = true)
                        }
                    }.hookAll {
                        intercept()
                    }
                }
            }
        }
    }
}
