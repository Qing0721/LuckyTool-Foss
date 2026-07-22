package com.fosstool.app.hook.scope.android

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.hasMethod
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.ModulePrefs

object BatteryOptimizationWhitelist : YukiBaseHooker() {
    override fun onHook() {
        val isEnable =
            prefs(ModulePrefs).getBoolean("restore_default_battery_optimization_whitelist", false)
        val disableCustom = false
        if (!isEnable) return

        "com.android.server.OplusDeviceIdleHelper".toClass().apply {
            method {
                name = if (hasMethod { name = "getNewWhiteList" }) "getNewWhiteList"
                else if (hasMethod { name = "getNewWhiteListLocked" }) "getNewWhiteListLocked"
                else return
                paramCount = 1
            }.hook {
                replaceUnit {
                    val whiteListAll = args().first().cast<java.util.ArrayList<String>>()
                    whiteListAll?.clear()
                    val mDefaultWhitelist =
                        field { name = "mDefaultWhitelist" }.get().list<String>()
                    whiteListAll?.addAll(mDefaultWhitelist)

                    if (!disableCustom) method { name = "getCustomizeWhiteList" }.get(instance)
                        .call(whiteListAll)
                    method { name = "addNfcJapanFelica" }.get(instance).call(whiteListAll)
                }
            }
        }
    }
}
