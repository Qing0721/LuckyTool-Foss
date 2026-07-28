package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import com.fosstool.app.hook.scope.weather.Enable15DayWeatherExpandList
import com.fosstool.app.hook.scope.weather.RemoveWeatherAD
import com.fosstool.app.hook.scope.weather.RestoreRainfallCloudMapPage
import com.fosstool.app.hook.utils.OplusBuildUtlils
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getAppSet

object HookWeather : YukiBaseHooker() {
    override fun onHook() {

        val weatherVersionCode = getAppSet(ModulePrefs, packageName)[1].toLongOrNull()
        val useNewImpl = when {
            weatherVersionCode == null -> {

                YLog.error(
                    "HookWeather: unknown weather app versionCode, fallback to sn2(RemoveWeatherAD)",
                    tag = "LuckyTool"
                )
                true
            }

            weatherVersionCode < 13000000L -> {
                YLog.error(
                    "HookWeather: legacy weather app ($weatherVersionCode < 13000000), " +
                        "sn0 implementation is not ported yet - weather AD hooks skipped",
                    tag = "LuckyTool"
                )
                false
            }

            else -> true
        }

        if (useNewImpl &&
            (prefs(ModulePrefs).getBoolean("remove_weather_some_page_bottom_ads", false) ||
                prefs(ModulePrefs).getBoolean("disable_weather_jump_browser", false))
        ) {
            loadHooker(RemoveWeatherAD)
        }
        if (prefs(ModulePrefs).getBoolean("enable_15_day_weather_expand_list", false)) {
            loadHooker(Enable15DayWeatherExpandList)
        }

        if (prefs(ModulePrefs).getBoolean("restore_rainfall_cloud_map_page", false)) {
            val os = try {
                OplusBuildUtlils(appClassLoader).getOSVersionCode ?: 0
            } catch (_: Throwable) {
                0
            }
            if (os < 34) loadHooker(RestoreRainfallCloudMapPage)
        }
    }
}
