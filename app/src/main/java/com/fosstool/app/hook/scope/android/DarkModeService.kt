package com.fosstool.app.hook.scope.android

import android.util.ArrayMap
import android.util.ArraySet
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.buildOf
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.ModulePrefs

object DarkModeService : YukiBaseHooker() {
    override fun onHook() {
        var isEnable = prefs(ModulePrefs).getBoolean("dark_mode_list_enable", false)
        dataChannel.wait<Boolean>("dark_mode_list_enable") { isEnable = it }
        var supportlistSet = prefs(ModulePrefs).getStringSet("dark_mode_support_list", ArraySet())
        dataChannel.wait<Set<String>>("dark_mode_support_list") { supportlistSet = it }

        val darkModeData = "com.oplus.darkmode.OplusDarkModeData".toClass()
        "com.android.server.OplusDarkModeServiceManager".toClass().apply {
            method { name = "updateList";paramCount = 1 }.hook {
                after {
                    if (!isEnable) return@after
                    val supportListMap = ArrayMap<String, Int>()
                    supportlistSet.forEach {
                        if (it.contains("|")) {
                            val arr = it.split("|").toMutableList()
                            if (arr.size < 2 || arr[1].isBlank()) arr[1] = (0).toString()
                            supportListMap[arr[0]] = arr[1].toInt()
                        } else supportListMap[it] = 0
                    }
                    val dataMap = ArrayMap<String, Any>()
                    supportListMap.forEach {
                        if (it.value == 0) dataMap[it.key] = darkModeData.buildOf { emptyParam() }
                        else dataMap[it.key] = darkModeData.buildOf { emptyParam() }?.current {
                            field { name = "mCurType" }.set(it.value)
                        }
                    }
                    field { name = "mRusAppMap" }.get(instance).set(dataMap.toMap())
                }
            }
        }
    }
}
