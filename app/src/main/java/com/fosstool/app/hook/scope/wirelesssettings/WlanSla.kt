package com.fosstool.app.hook.scope.wirelesssettings

import android.util.ArraySet
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClass
import com.highcapable.yukihookapi.hook.log.YLog
import com.fosstool.app.utils.ModulePrefs

object WlanSla : YukiBaseHooker() {
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

        val targetClass = try {
            "com.oplus.server.wifi.sla.OplusSlaApps".toClass()
        } catch (e: Throwable) {
            try {
                "com.oplus.server.wifi.OplusSlaApps".toClass()
            } catch (e2: Throwable) {
                YLog.error("WlanSla: OplusSlaApps class not found", tag = "LuckyTool")
                return
            }
        }

        targetClass.apply {
            method { name = "getSlaWhiteListAppsFromRus" }.hook {
                after {
                    if (mode == "0") return@after
                    val original = result<Any>()
                    if (original is Array<*>) {
                        val originalList = original.mapNotNull { it as? String }.toMutableList()
                        when (mode) {
                            "1" -> {
                                whitelist.forEach { pkg ->
                                    if (!originalList.contains(pkg)) originalList.add(pkg)
                                }
                                result = originalList.toTypedArray()
                            }
                            "2" -> {
                                result = whitelist.toTypedArray()
                            }
                        }
                    }
                }
            }
            method { name = "getSlaGameAppsFromRus" }.hook {
                after {
                    if (mode == "0") return@after
                    val original = result<Any>()
                    if (original is Array<*>) {
                        val originalList = original.mapNotNull { it as? String }.toMutableList()
                        when (mode) {
                            "1" -> {
                                gameWhitelist.forEach { pkg ->
                                    if (!originalList.contains(pkg)) originalList.add(pkg)
                                }
                                result = originalList.toTypedArray()
                            }
                            "2" -> {
                                result = gameWhitelist.toTypedArray()
                            }
                        }
                    }
                }
            }
            method { name = "getSlaBlackListAppsFromRus" }.hook {
                after {
                    if (mode != "0" && removeBlacklist) {
                        result = null
                    }
                }
            }
        }
    }
}
