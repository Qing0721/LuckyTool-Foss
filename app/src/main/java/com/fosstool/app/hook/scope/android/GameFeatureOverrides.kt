package com.fosstool.app.hook.scope.android

import android.util.ArrayMap
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClass
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getOSVersionCode

object GameFeatureOverrides : YukiBaseHooker() {
    override fun onHook() {
        val enableGameAcceleration =
            prefs(ModulePrefs).getBoolean("enable_game_acceleration", false)
        val enableGameArchitectureDisplay =
            prefs(ModulePrefs).getBoolean("enable_game_architecture_display", false)

        if (!enableGameAcceleration && !enableGameArchitectureDisplay) return

        val featureMap = ArrayMap<String, Boolean>()

        if (enableGameAcceleration && getOSVersionCode >= 30) {
            featureMap["oplus.software.game.cold.start.speedup.enable"] = true
        }

        if (enableGameArchitectureDisplay && getOSVersionCode >= 34) {
            featureMap["oplus.software.game.hummingbird"] = true
        }

        if (featureMap.isEmpty()) return

        try {
            "com.oplus.content.OplusFeatureConfigManager".toClass().apply {
                method {
                    name = "hasFeature"
                    param(StringClass)
                    returnType = BooleanType
                }.hook {
                    before {
                        val key = args().first().string()
                        if (key.isNotEmpty()) {
                            featureMap[key]?.let { result = it }
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            YLog.error("GameFeatureOverrides: OplusFeatureConfigManager not found", tag = "LuckyTool")
        }

        try {
            "com.android.server.content.OplusFeatureConfigManagerService".toClass().apply {
                method {
                    name = "hasFeature"
                    param(StringClass)
                    returnType = BooleanType
                }.hook {
                    before {
                        val key = args().first().string()
                        if (key.isNotEmpty()) {
                            featureMap[key]?.let { result = it }
                        }
                    }
                }
            }
        } catch (e: Throwable) {
        }
    }
}
