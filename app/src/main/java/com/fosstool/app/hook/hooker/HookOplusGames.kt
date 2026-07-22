package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.ListClass
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.fosstool.app.hook.scope.oplusgames.CloudConditionFeature
import com.fosstool.app.hook.scope.oplusgames.CompetitionModeSound
import com.fosstool.app.hook.scope.oplusgames.CustomBarrageWhitelist
import com.fosstool.app.hook.scope.oplusgames.CustomMediaPlayerSupport
import com.fosstool.app.hook.scope.oplusgames.EnableDeveloperPage
import com.fosstool.app.hook.scope.oplusgames.EnableSupportCompetitionMode
import com.fosstool.app.hook.scope.oplusgames.RemoveGameAssistantTemperatureDetection
import com.fosstool.app.hook.scope.oplusgames.RemoveRootCheck
import com.fosstool.app.hook.scope.oplusgames.RemoveSomeVipLimit
import com.fosstool.app.hook.scope.oplusgames.RemoveStartupAnimation
import com.fosstool.app.hook.scope.oplusgames.RemoveToolRecommendationCard
import com.fosstool.app.hook.scope.exsystemservice.EnableGameRunInBackground
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getAppSet
import com.fosstool.app.utils.getOSVersionCode

object HookOplusGames : YukiBaseHooker() {
    override fun onHook() {
        if (packageName == "com.oplus.games") {
            val appSet = getAppSet(ModulePrefs, packageName)


            loadHooker(CloudConditionFeature(appSet))

            loadHooker(CustomMediaPlayerSupport)

            loadHooker(RemoveToolRecommendationCard)

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
            if (prefs(ModulePrefs).getBoolean("remove_welfare_page", false)) {
                val mainPanel = runCatching {
                    "business.mainpanel.MainPanelView".toClass()
                }.getOrNull()
                if (mainPanel != null) {
                    mainPanel.apply {
                        method {
                            param { it[0] == ListClass && it[1] == BooleanType }
                            paramCount(2..3)
                            returnType = UnitType
                        }.hook {
                            before {
                                val list = args().first().list<Any>()
                                if (list.isEmpty()) return@before
                                args().first().set(java.util.ArrayList<Any>().apply { add(list.first()) })
                            }
                        }
                    }
                } else {
                    runCatching {
                        "business.mainpanel.main.MainPanelFragment".toClass().apply {
                            method { name = "addRadioButton" }.hookAll {
                                after {
                                    val key = runCatching { args().first().string() }.getOrNull()
                                        ?: return@after
                                    if (key == "welfare") resultNull()
                                }
                            }
                        }
                    }
                    runCatching {
                        "business.mainpanel.view.NavigationRadioButton".toClass().apply {
                            method {
                                paramCount = 3
                                returnType = UnitType
                            }.hookAll {
                                after {
                                    val view = instance as? android.view.View ?: return@after
                                    val tag = view.tag?.toString().orEmpty()
                                    if (tag.contains("welfare", ignoreCase = true)) {
                                        view.visibility = android.view.View.GONE
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (prefs(ModulePrefs).getBoolean("remove_some_vip_limit", false)) {
                loadHooker(RemoveSomeVipLimit)
            }
            if (prefs(ModulePrefs).getBoolean("remove_game_assistant_temperature_detection")) {
                loadHooker(RemoveGameAssistantTemperatureDetection)
            }
            loadHooker(CustomBarrageWhitelist)

            if (prefs(ModulePrefs).getBoolean("enable_game_run_in_background", false) && getOSVersionCode >= 27) {
                loadHooker(EnableGameRunInBackground)
            }







        }

    }
}
