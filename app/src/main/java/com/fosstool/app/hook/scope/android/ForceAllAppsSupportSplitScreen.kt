package com.fosstool.app.hook.scope.android

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.fosstool.app.utils.ModulePrefs
import de.robv.android.xposed.XposedHelpers

object ForceAllAppsSupportSplitScreen : YukiBaseHooker() {
    override fun onHook() {
        var isEnable = prefs(ModulePrefs).getBoolean("force_all_apps_support_split_screen", false)
        dataChannel.wait<Boolean>("force_all_apps_support_split_screen") { isEnable = it }

        val mgr = "com.android.server.wm.OplusSplitScreenManagerService"
            .toClassOrNull(appClassLoader)
        if (mgr == null) {
            YLog.error("ForceAllAppsSupportSplitScreen: OplusSplitScreenManagerService not found")
            return
        }

        mgr.method {
            name = "supportsSplitScreenByVendorPolicy"
            paramCount(3..4)
            param { it.size >= 2 && it[0] == StringClass && it[1] == StringClass }
        }.ignored().hookAll {
            before {
                if (!isEnable) return@before
                val pkgName = args(0).any() as? String ?: ""
                val activityName = args(1).any() as? String ?: ""
                if (pkgName.isBlank()) return@before

                val isSafe = runCatching {
                    XposedHelpers.callMethod(instance, "isSafeSenterUI", activityName) as? Boolean
                }.getOrNull() ?: false
                if (isSafe) return@before

                val paramCount = runCatching { method.parameterCount }.getOrDefault(-1)
                if (paramCount != 4) return@before

                val userId = args().last().any() as? Int ?: 0
                val hidden = runCatching {
                    XposedHelpers.callMethod(
                        instance, "isHidenPackage", pkgName, userId
                    ) as? Boolean
                }.getOrNull() ?: false
                if (!hidden) result = true
            }
        }

        mgr.method { name = "isInForbidActivityList" }.ignored().hook {
            before { if (isEnable) resultFalse() }
        }
        mgr.method { name = "supportsSplitScreenWindowingMode" }.ignored().hook {
            before { if (isEnable) resultTrue() }
        }
    }
}
