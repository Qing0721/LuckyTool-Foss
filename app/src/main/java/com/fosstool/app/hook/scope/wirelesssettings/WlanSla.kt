package com.fosstool.app.hook.scope.wirelesssettings

import android.util.ArraySet
import com.fosstool.app.hook.scope.android.HookNotificationManager
import com.fosstool.app.utils.ModulePrefs
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog

object WlanSla : YukiBaseHooker() {

    @Volatile
    var wifiClassLoader: ClassLoader? = null

    private val STRING_ARRAY: Class<*> = Array<String>::class.java

    override fun onHook() {
        var mode = prefs(ModulePrefs).getString("set_wlan_sla_whitelist_mode", "0")
        dataChannel.wait<String>("set_wlan_sla_whitelist_mode") { mode = it }
        var removeBlacklist = prefs(ModulePrefs).getBoolean("remove_wlan_sla_blacklist", false)
        dataChannel.wait<Boolean>("remove_wlan_sla_blacklist") { removeBlacklist = it }
        var whitelist = prefs(ModulePrefs).getStringSet("custom_wlan_sla_whitelist", ArraySet())
        dataChannel.wait<Set<String>>("custom_wlan_sla_whitelist") { whitelist = it }
        var gameWhitelist = prefs(ModulePrefs).getStringSet("custom_wlan_sla_game_whitelist", ArraySet())
        dataChannel.wait<Set<String>>("custom_wlan_sla_game_whitelist") { gameWhitelist = it }

        if (mode == "0") return

        val wifiLoader = wifiClassLoader ?: loadOplusWifiClassLoader()
        if (wifiLoader == null) {
            YLog.error("WlanSla: oplus wifi service ClassLoader is null", tag = "LuckyTool")
        }

        val targetClass = VariousClass(
            "com.oplus.server.wifi.OplusSlaApps",
            "com.oplus.server.wifi.sla.OplusSlaApps",
        ).toClassOrNull(wifiLoader ?: appClassLoader)
        if (targetClass == null) {
            YLog.error("WlanSla: OplusSlaApps class not found", tag = "LuckyTool")
            return
        }

        targetClass.method {
            name = "getSlaWhiteListAppsFromRus"
            returnType = STRING_ARRAY
        }.ignored().hook {
            after {
                if (mode == "0") return@after
                val original = result as? Array<*> ?: return@after
                val originalList = original.mapNotNull { it as? String }.toMutableList()
                when (mode) {
                    "1" -> {
                        whitelist.forEach { pkg ->
                            if (!originalList.contains(pkg)) originalList.add(pkg)
                        }
                        result = originalList.toTypedArray()
                    }
                    "2" -> result = whitelist.toTypedArray()
                }
            }
        }

        targetClass.method {
            name = "getSlaGameAppsFromRus"
            returnType = STRING_ARRAY
        }.ignored().hook {
            after {
                if (mode == "0") return@after
                val original = result as? Array<*> ?: return@after
                val originalList = original.mapNotNull { it as? String }.toMutableList()
                when (mode) {
                    "1" -> {
                        gameWhitelist.forEach { pkg ->
                            if (!originalList.contains(pkg)) originalList.add(pkg)
                        }
                        result = originalList.toTypedArray()
                    }
                    "2" -> result = gameWhitelist.toTypedArray()
                }
            }
        }

        targetClass.method {
            name = "getSlaBlackListAppsFromRus"
        }.ignored().hook {
            before {
                if (mode != "0" && removeBlacklist) resultNull()
            }
        }
    }

    private fun loadOplusWifiClassLoader(): ClassLoader? =
        runCatching { HookNotificationManager.oplusWifiClassLoader(appClassLoader) }.getOrNull()
}
