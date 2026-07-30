package com.fosstool.app.hook.scope.android

import android.util.ArrayMap
import android.util.ArraySet
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.utils.ModulePrefs
import de.robv.android.xposed.XposedHelpers

object DarkModeService : YukiBaseHooker() {
    override fun onHook() {
        var isEnable = prefs(ModulePrefs).getBoolean("dark_mode_list_enable", false)
        dataChannel.wait<Boolean>("dark_mode_list_enable") { isEnable = it }
        var supportlistSet = prefs(ModulePrefs).getStringSet("dark_mode_support_list", ArraySet())
        dataChannel.wait<Set<String>>("dark_mode_support_list") { supportlistSet = it }

        val darkModeData = "com.oplus.darkmode.OplusDarkModeData".toClassOrNull(appClassLoader)
        val mgr = "com.android.server.OplusDarkModeServiceManager".toClassOrNull(appClassLoader) ?: return

        mgr.method { name { it.startsWith("updateList") }; paramCount = 1 }.ignored().hook {
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
                supportListMap.forEach { (pkg, type) ->
                    val data = darkModeData?.let {
                        runCatching { XposedHelpers.newInstance(it) }.getOrNull()
                    }
                    if (data != null && type != 0) {
                        runCatching { XposedHelpers.setIntField(data, "mCurType", type) }
                    }
                    if (data != null) dataMap[pkg] = data
                }
                mgr.field { name = "mRusAppMap" }.ignored().get(instance).set(dataMap.toMap())
            }
        }
    }
}
