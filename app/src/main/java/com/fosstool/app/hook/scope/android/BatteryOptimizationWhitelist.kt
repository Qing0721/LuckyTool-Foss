package com.fosstool.app.hook.scope.android

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog
import com.fosstool.app.utils.ModulePrefs
import de.robv.android.xposed.XposedHelpers

object BatteryOptimizationWhitelist : YukiBaseHooker() {
    override fun onHook() {
        val isEnable =
            prefs(ModulePrefs).getBoolean("restore_default_battery_optimization_whitelist", false)
        val disableCustom = false
        if (!isEnable) return

        val cls = "com.android.server.OplusDeviceIdleHelper".toClassOrNull(appClassLoader)
        if (cls == null) {
            YLog.error("BatteryOptimizationWhitelist: OplusDeviceIdleHelper not found")
            return
        }

        val hasNewWhiteList = cls.method {
            name = "getNewWhiteList"
            superClass()
        }.ignored().give() != null

        val targetName = if (hasNewWhiteList) "getNewWhiteList" else "getNewWhiteListLocked"

        cls.method {
            name = targetName
            superClass()
        }.ignored().hook {
            before {
                @Suppress("UNCHECKED_CAST")
                val whiteListAll = args(0).any() as? java.util.ArrayList<String> ?: return@before
                whiteListAll.clear()
                @Suppress("UNCHECKED_CAST")
                val mDefaultWhitelist = runCatching {
                    cls.field { name = "mDefaultWhitelist"; superClass() }
                        .ignored().get(instance).any() as? List<String>
                }.getOrNull()
                if (mDefaultWhitelist != null) whiteListAll.addAll(mDefaultWhitelist)

                if (!disableCustom) {
                    runCatching {
                        XposedHelpers.callMethod(instance, "getCustomizeWhiteList", whiteListAll)
                    }
                }
                runCatching {
                    XposedHelpers.callMethod(instance, "addNfcJapanFelica", whiteListAll)
                }
                resultNull()
            }
        }
    }
}
