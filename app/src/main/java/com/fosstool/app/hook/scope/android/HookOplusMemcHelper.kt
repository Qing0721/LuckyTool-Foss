package com.fosstool.app.hook.scope.android

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog
import kotlinx.serialization.json.Json
import com.fosstool.app.data.MemcConfigActivity
import com.fosstool.app.data.MemcConfigPackage
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getOSVersionCode

object HookOplusMemcHelper : YukiBaseHooker() {

    private val json = Json

    private val packageList = ArrayList<MemcConfigPackage>()
    private val activityList = ArrayList<MemcConfigActivity>()

    private val packNames = ArrayList<String>()
    private val packRateMap = HashMap<String, String>()
    private val packTypeMap = HashMap<String, String>()
    private val activities = ArrayList<String>()
    private val activityTypeMap = HashMap<String, String>()

    override fun onHook() {
        if (getOSVersionCode < 26) return
        val enable = prefs(ModulePrefs).getBoolean("enable_video_memc_frame_insertion", false)
        if (!enable) return

        val configPackages = prefs(ModulePrefs).getStringSet("memc_config_package_list", emptySet())
        val configActivitys = prefs(ModulePrefs).getStringSet("memc_config_activity_list", emptySet())

        val clazz = "com.android.server.display.memc.OplusMemcHelper".toClassOrNull()
            ?: "com.android.server.display.feature.vrr.memc.OplusMemcHelper".toClassOrNull()
        if (clazz == null) {
            YLog.error("HookOplusMemcHelper: OplusMemcHelper not found", tag = "LuckyTool")
            return
        }

        try {
            clazz.apply {
                method { name = "init" }.hook {
                    after { populateCaches(configPackages, configActivitys) }
                }
                method { name = "getConfigAppList" }.hook {
                    after { if (packNames.isNotEmpty()) result = packNames }
                }
                method { name = "getAppScreenRateMap" }.hook {
                    after { if (packRateMap.isNotEmpty()) result = packRateMap }
                }
                method { name = "getSdr2hdrCommandMap" }.hook {
                    after { if (packTypeMap.isNotEmpty()) result = packTypeMap }
                }
                method { name = "getConfigActivityList" }.hook {
                    after { if (activities.isNotEmpty()) result = activities }
                }
                method { name = "getMemcCommandMap" }.hook {
                    after { if (activityTypeMap.isNotEmpty()) result = activityTypeMap }
                }
            }
        } catch (e: Throwable) {
            YLog.error("HookOplusMemcHelper: hook failed", e, tag = "LuckyTool")
        }
    }

    private fun populateCaches(
        configPackages: Set<String>, configActivitys: Set<String>
    ) {
        packageList.clear()
        activityList.clear()
        for (str in configPackages) {
            val v = runCatching { json.decodeFromString<MemcConfigPackage>(str) }.getOrNull()
            if (v != null) packageList.add(v)
        }
        for (str in configActivitys) {
            val v = runCatching { json.decodeFromString<MemcConfigActivity>(str) }.getOrNull()
            if (v != null) activityList.add(v)
        }
        packNames.clear()
        packRateMap.clear()
        packTypeMap.clear()
        for (v in packageList) {
            packNames.add(v.packName)
            packRateMap[v.packName] = v.rate
            packTypeMap[v.packName] = v.type
        }
        activities.clear()
        activityTypeMap.clear()
        for (v in activityList) {
            activities.add(v.activity)
            activityTypeMap[v.packName + "/" + v.activity] = v.type
        }
    }
}
