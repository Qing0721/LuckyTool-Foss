package com.fosstool.app.hook.scope.android

import android.util.ArrayMap
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClass
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK
import com.fosstool.app.utils.getOSVersionCode

object SuperVolumeMode : YukiBaseHooker() {
    override fun onHook() {
        val enableSuperVolume = prefs(ModulePrefs).getBoolean("enable_super_volume_mode", false)
        val enableCallSuperVolume =
            prefs(ModulePrefs).getBoolean("enable_super_volume_mode_for_calls", false)

        if (!enableSuperVolume && !enableCallSuperVolume) return

        val featureMap = ArrayMap<String, Boolean>()

        if (enableSuperVolume && SDK >= 33) {
            featureMap["oplus.software.audio.super_volume"] = true
            featureMap["oplus.software.audio.super_volume_3x"] = true
        }

        if (enableCallSuperVolume && getOSVersionCode >= 27) {
            featureMap["oplus.software.audio.super_volume_call_earpiece"] = true
            featureMap["oplus.software.audio.super_volume_call_earpiece_disable"] = false
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
            YLog.error("SuperVolumeMode: OplusFeatureConfigManager not found", tag = "LuckyTool")
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
                try {
                    method {
                        name = "hasFeatureMap"
                        param(StringClass, IntType)
                        returnType = BooleanType
                    }.hook {
                        before {
                            val key = args().first().string()
                            if (key.isNotEmpty()) {
                                featureMap[key]?.let { result = it }
                            }
                        }
                    }
                } catch (e: Throwable) {
                }
            }
        } catch (e: Throwable) {
        }
    }
}
