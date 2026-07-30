package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.oplusgames.CloudConditionFeature
import com.fosstool.app.hook.scope.oplusgames.CompetitionModeSound
import com.fosstool.app.hook.scope.oplusgames.CustomBarrageWhitelist
import com.fosstool.app.hook.scope.oplusgames.CustomMediaPlayerSupport
import com.fosstool.app.hook.scope.oplusgames.EnableDeveloperPage
import com.fosstool.app.hook.scope.oplusgames.EnableSupportCompetitionMode
import com.fosstool.app.hook.scope.oplusgames.EnableXModeFeature
import com.fosstool.app.hook.scope.oplusgames.RemoveGameAssistantTemperatureDetection
import com.fosstool.app.hook.scope.oplusgames.RemoveRootCheck
import com.fosstool.app.hook.scope.oplusgames.RemoveSomeVipLimit
import com.fosstool.app.hook.scope.oplusgames.RemoveStartupAnimation
import com.fosstool.app.hook.scope.oplusgames.RemoveToolRecommendationCard
import com.fosstool.app.hook.scope.oplusgames.RemoveWelfarePage
import com.fosstool.app.hook.scope.exsystemservice.EnableGameRunInBackground
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getAppSet
import com.fosstool.app.utils.getOSVersionCode
import com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker
import com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker.Mode

class HookOplusGames : YukiBaseHooker() {
    override fun onHook() {
        if (packageName == "com.oplus.cosa") {
            loadHooker(SystemPropertiesOverrideEngineHooker(mode = Mode.RM0_Q))
            return
        }
        if (packageName == "com.oplus.games") {
            val appSet = getAppSet(ModulePrefs, packageName)

            if (appSet[2] == "0") return

            val majorVersion = appSet[0].substringBefore(".").toIntOrNull() ?: 10
            val versionCode = appSet[1].toLongOrNull() ?: 0L

            loadHooker(SystemPropertiesOverrideEngineHooker(mode = Mode.RM0_Q))

            if (majorVersion < 10) loadHooker(CloudConditionFeature(appSet))

            loadHooker(CustomMediaPlayerSupport)

            if (majorVersion < 10 && versionCode >= 90000000L) {
                loadHooker(RemoveToolRecommendationCard)
            }

            if (prefs(ModulePrefs).getBoolean("remove_root_check", false)) {
                loadHooker(RemoveRootCheck)
            }
            if (prefs(ModulePrefs).getBoolean("remove_startup_animation", false)) {
                loadHooker(RemoveStartupAnimation)
            }
            if (prefs(ModulePrefs).getBoolean("enable_developer_page", false)) {
                loadHooker(EnableDeveloperPage)
            }
            if (prefs(ModulePrefs).getBoolean("enable_support_competition_mode", false)) {
                loadHooker(EnableSupportCompetitionMode)
            }
            if (prefs(ModulePrefs).getBoolean("remove_competition_mode_sound", false)) {
                loadHooker(CompetitionModeSound)
            }

            if (prefs(ModulePrefs).getBoolean("remove_welfare_page", false) && majorVersion < 10) {
                loadHooker(RemoveWelfarePage)
            }
            if (prefs(ModulePrefs).getBoolean("remove_some_vip_limit", false)) {
                loadHooker(RemoveSomeVipLimit)
            }
            if (prefs(ModulePrefs).getBoolean("remove_game_assistant_temperature_detection")) {
                loadHooker(RemoveGameAssistantTemperatureDetection)
            }
            if (prefs(ModulePrefs).getBoolean("enable_x_mode_feature", false)) {
                loadHooker(EnableXModeFeature)
            }
            loadHooker(CustomBarrageWhitelist)

            if (prefs(ModulePrefs).getBoolean("enable_game_run_in_background", false) && getOSVersionCode >= 27) {
                loadHooker(EnableGameRunInBackground())
            }

        }

    }
}
