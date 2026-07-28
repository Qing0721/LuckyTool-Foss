package com.fosstool.app.hook.scope.oplusgames

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.AnyClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.ListClass
import com.highcapable.yukihookapi.hook.type.java.MapClass
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.fosstool.app.utils.ModulePrefs
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

class CloudConditionFeature(private val appSet: Array<String>) : YukiBaseHooker() {
    override fun onHook() {
        loadHooker(HookOplusFeature)
        loadHooker(HookCloudCondition)

        val versionCode = appSet[1].toIntOrNull()?.takeIf { it > 80130000 } ?: 0
        if (versionCode > 80130000) loadHooker(HookCloudApiImpl)
    }

    private object HookOplusFeature : YukiBaseHooker() {
        override fun onHook() {
            val gpuControl = prefs(ModulePrefs).getBoolean("enable_adreno_gpu_controller", false)
            val pickleFeature =
                prefs(ModulePrefs).getBoolean("enable_increase_fps_limit_feature", false)
            val fpsFeature = prefs(ModulePrefs).getBoolean("enable_increase_fps_feature", false)
            val powerFeature = prefs(ModulePrefs).getBoolean("enable_optimise_power_feature", false)
            val gtMode = prefs(ModulePrefs).getBoolean("enable_gt_mode_feature", false)
            val superResolution =
                prefs(ModulePrefs).getBoolean("enable_super_resolution_feature", false)
            val xMode = prefs(ModulePrefs).getBoolean("enable_x_mode_feature", false)

            ("com.oplus.addon.OplusFeatureHelper\$Companion".toClassOrNull(appClassLoader)
                ?: "com.oplus.addon.OplusFeatureHelper".toClassOrNull(appClassLoader))?.apply {
                method {
                    param(StringClass, BooleanType)
                    returnType = BooleanType
                }.hook {
                    after {
                        when (args().first().string()) {
                            "oplus.software.display.game.memc_enable" -> if (pickleFeature || fpsFeature || powerFeature) resultTrue()
                            "oplus.software.display.game.memc_increase_fps_limit_mode" -> if (pickleFeature) resultTrue()
                            "oplus.software.display.game.memc_increase_fps_mode" -> if (fpsFeature) resultTrue()
                            "oplus.software.display.game.memc_optimise_power_mode" -> if (powerFeature) resultTrue()
                            "oplus.gpu.controlpanel.support" -> if (gpuControl) resultTrue()
                            "oplus.software.support.gt.mode" -> if (gtMode) resultTrue()
                            "oplus.software.display.game.sr_enable" -> if (superResolution) resultTrue()
                            "oplus.software.display.game.sr.fully_enable" -> if (superResolution) resultTrue()
                            "oplus.software.general.cooling.back.clip.enable" -> if (xMode) resultTrue()

                        }
                    }
                }
            }
        }
    }

    private object HookCloudCondition : YukiBaseHooker() {
        override fun onHook() {
            val gpuControl = prefs(ModulePrefs).getBoolean("enable_adreno_gpu_controller", false)
            val pickleFeature =
                prefs(ModulePrefs).getBoolean("enable_increase_fps_limit_feature", false)
            val fpsFeature = prefs(ModulePrefs).getBoolean("enable_increase_fps_feature", false)
            val powerFeature = prefs(ModulePrefs).getBoolean("enable_optimise_power_feature", false)
            val xMode = prefs(ModulePrefs).getBoolean("enable_x_mode_feature", false)
            val superResolution =
                prefs(ModulePrefs).getBoolean("enable_super_resolution_feature", false)
            val oneplusCharacteristic =
                prefs(ModulePrefs).getBoolean("enable_one_plus_characteristic", false)
            val magicVoice =
                prefs(ModulePrefs).getBoolean("remove_game_voice_changer_whitelist", false)
            val gameAiPlay = prefs(ModulePrefs).getBoolean("enable_game_ai_play", false)

            "com.coloros.gamespaceui.config.cloud.CloudConditionUtil".toClassOrNull(appClassLoader)?.apply {
                method {
                    param(StringClass, MapClass, IntType, AnyClass)
                    returnType = BooleanType
                }.hook {
                    before {
                        when (args().first().string()) {
                            "frame_insert" -> if (pickleFeature) resultTrue()
                            "increase_fps" -> if (fpsFeature) resultTrue()
                            "optimise_power" -> if (powerFeature) resultTrue()
                            "gpu_control_panel" -> if (gpuControl) resultTrue()
                            "cool_back_clip_blacklist" -> if (xMode) resultTrue()
                            "one_plus_characteristic" -> if (oneplusCharacteristic) resultTrue()
                            "game_ai_play_key" -> if (gameAiPlay) resultTrue()
                        }
                    }
                }
                method {
                    param(StringClass, MapClass)
                    returnType = BooleanType
                }.hook {
                    before {
                        when (args().first().string()) {
                            "super_resolution_config" -> if (superResolution) resultTrue()
                        }
                    }
                }
                method {
                    param { it[0] == StringClass && it[1] == MapClass }
                    paramCount = 3
                }.hook {
                    after {
                        when (args().first().string()) {
                            "magic_voice_config" -> if (magicVoice) resultTrue()
                        }
                    }
                }
            }
        }
    }

    private object HookCloudApiImpl : YukiBaseHooker() {
        override fun onHook() {
            val gpuControl = prefs(ModulePrefs).getBoolean("enable_adreno_gpu_controller", false)
            val superResolution =
                prefs(ModulePrefs).getBoolean("enable_super_resolution_feature", false)
            val oneplusCharacteristic =
                prefs(ModulePrefs).getBoolean("enable_one_plus_characteristic", false)

            DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
                dexKitBridge.findClass {
                    matcher {
                        usingStrings("cloudKey", "defaultDate", "spFileName")
                        methods {
                            add { paramCount(0);returnType(ListClass.name) }
                            add { paramCount(1);returnType(ListClass.name) }
                            add { paramCount(2);returnType(BooleanType.name) }
                        }
                    }
                }.apply {
                    checkDataList("HookCloudApiImpl")
                    val member = firstOrNullSafe() ?: return@apply
                    member.name.toClassOrNull(appClassLoader)?.apply {
                        method {
                            name = "isFunctionEnabledFromCloud"
                            paramCount = 2
                        }.hook {
                            before {
                                when (args().first().string()) {
                                    "gpu_control_panel" -> if (gpuControl) resultTrue()
                                    "one_plus_characteristic" -> if (oneplusCharacteristic) resultTrue()
                                    "super_resolution_config" -> if (superResolution) resultTrue()
                                    "super_resolution_config_full" -> if (superResolution) resultTrue()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
