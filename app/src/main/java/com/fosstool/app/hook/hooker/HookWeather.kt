package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.weather.Enable15DayWeatherExpandList
import com.fosstool.app.hook.scope.weather.RemoveWeatherAD
import com.fosstool.app.hook.scope.weather.RestoreRainfallCloudMapPage
import com.fosstool.app.utils.ModulePrefs

object HookWeather : YukiBaseHooker() {
    override fun onHook() {
        if (prefs(ModulePrefs).getBoolean("remove_weather_some_page_bottom_ads", false) ||
            prefs(ModulePrefs).getBoolean("disable_weather_jump_browser", false)
        ) {
            loadHooker(RemoveWeatherAD)
        }
        if (prefs(ModulePrefs).getBoolean("enable_15_day_weather_expand_list", false)) {
            loadHooker(Enable15DayWeatherExpandList)
        }
        if (prefs(ModulePrefs).getBoolean("restore_rainfall_cloud_map_page", false)) {
            loadHooker(RestoreRainfallCloudMapPage)
        }
    }
}
