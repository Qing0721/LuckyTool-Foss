package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.A14
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK

object HookSystemUIFeature : YukiBaseHooker() {
    override fun onHook() {
        loadHooker(HookFeatureOption)
        loadHooker(HookStatusBarFeature)
        loadHooker(HookNotificationAppFeature)
        loadHooker(HookFlavorOneFeature)
        loadHooker(HookQSFeatureOption)
        if (SDK >= A14) {
            loadHooker(HookVolumeFeatureOption)
        }
    }

    private object HookFeatureOption : YukiBaseHooker() {
        override fun onHook() {
            val autoBrightnessMode =
                prefs(ModulePrefs).getString("set_auto_brightness_button_mode", "0")
            val fullScreenChargeAnim =
                prefs(ModulePrefs).getString("set_full_screen_charging_animation_mode", "0")
            val notifyImportance = prefs(ModulePrefs).getBoolean(
                "enable_notification_importance_classification", false
            )
            val enableBlur =
                prefs(ModulePrefs).getBoolean("force_enable_systemui_blur_feature", false)
            val volumePosition =
                prefs(ModulePrefs).getString("set_volume_bar_display_position", "0")
            var volumeBlur =
                prefs(ModulePrefs).getInt("custom_volume_dialog_background_transparency", -1)
            dataChannel.wait<Int>("custom_volume_dialog_background_transparency") {
                volumeBlur = it
            }
            var showWattage =
                prefs(ModulePrefs).getBoolean("force_lock_screen_charging_show_wattage", false)
            dataChannel.wait<Boolean>("force_lock_screen_charging_show_wattage") {
                showWattage = it
            }
            var warpCharge =
                prefs(ModulePrefs).getString("set_lock_screen_warp_charging_style", "0")
            dataChannel.wait<String>("set_lock_screen_warp_charging_style") { warpCharge = it }
            val removeMyDevice =
                prefs(ModulePrefs).getBoolean("remove_control_center_mydevice", false)
            val forceClockStyle =
                prefs(ModulePrefs).getBoolean("force_display_clock_style_options", false)

            val clazz = "com.oplusos.systemui.common.feature.FeatureOption".toClassOrNull(appClassLoader) ?: return

            clazz.method { name = "shouldRemoveAutoBrightness" }.ignored().hook {
                before {
                    when (autoBrightnessMode) {
                        "1" -> result = false
                        "2" -> result = true
                    }
                }
            }
            if (notifyImportance) {
                clazz.method { name = "isOriginNotificationBehavior" }.ignored().hook {
                    replaceToTrue()
                }
            }
            if (volumeBlur > -1) {
                clazz.method { name = "isVolumeBlurDisabled" }.ignored().hook {
                    replaceToFalse()
                }
            }
            if (enableBlur) {
                clazz.method { name = "isAiSdr2HdrSupport" }.ignored().hook {
                    replaceToFalse()
                }
            }
            clazz.method { name = "isOplusVolumeKeyInRight" }.ignored().hook {
                before {
                    when (volumePosition) {
                        "1" -> result = false
                        "2" -> result = true
                    }
                }
            }
            clazz.method { name = "areVolumeAndPowerKeysInRight" }.ignored().hook {
                before {
                    when (volumePosition) {
                        "1" -> result = false
                        "2" -> result = true
                    }
                }
            }
            clazz.method { name = "isSupportFullScreenChargeAnim" }.ignored().hook {
                before {
                    when (fullScreenChargeAnim) {
                        "1" -> result = true
                        "2" -> result = false
                    }
                }
            }
            if (warpCharge == "2" && showWattage) {
                clazz.method { name = "isSupportShowWattage" }.ignored().hook {
                    replaceToTrue()
                }
            }
            if (SDK == A13) {
                clazz.method { name = "isUseWarpCharge" }.ignored().hook {
                    before {
                        when (warpCharge) {
                            "1" -> result = true
                            "2" -> result = false
                        }
                    }
                }
            }
            if (removeMyDevice) {
                clazz.method { name = "isSupportMyDevice" }.ignored().hook {
                    replaceToFalse()
                }
            }
            if (forceClockStyle && SDK < A13) {
                clazz.method { name = "isSupportLandClock" }.ignored().hook {
                    replaceToTrue()
                }
            }
        }
    }

    private object HookStatusBarFeature : YukiBaseHooker() {
        override fun onHook() {
            val hideSignalLabels =
                prefs(ModulePrefs).getBoolean("hide_inactive_signal_labels_gen2x2", false)
            if (!hideSignalLabels) return

            val clazz = VariousClass(
                "com.oplusos.systemui.statusbar.feature.StatusBarFeatureOption",
                "com.oplusos.systemui.common.feature.StatusBarFeatureOption",
            ).toClassOrNull(appClassLoader) ?: return

            clazz.method { name = "loadAppFeature" }.ignored().hook {
                after {
                    runCatching {
                        val field = clazz.declaredFields.firstOrNull { it.name == "isSystemUiExpSignalUi" }
                            ?: clazz.superclass?.declaredFields?.firstOrNull { it.name == "isSystemUiExpSignalUi" }
                        field?.isAccessible = true
                        field?.set(instance, true)
                    }
                }
            }
        }
    }

    private object HookNotificationAppFeature : YukiBaseHooker() {
        override fun onHook() {
            val notifyImportance = prefs(ModulePrefs).getBoolean(
                "enable_notification_importance_classification", false
            )
            val enableBlur =
                prefs(ModulePrefs).getBoolean("force_enable_systemui_blur_feature", false)

            val clazz = VariousClass(
                "com.oplusos.systemui.common.util.NotificationAppFeatureOption",
                "com.oplusos.systemui.common.feature.NotificationFeatureOption",
            ).toClassOrNull(appClassLoader) ?: return

            val notifyMethod = if (SDK >= A14) "isOriginNotificationBehavior"
            else "originNotificationBehavior"
            if (notifyImportance) {
                clazz.method { name = notifyMethod }.ignored().hook {
                    replaceToTrue()
                }
            }
            if (enableBlur && SDK >= A13) {
                val blurMethod = if (SDK >= A14) "isGaussBlurDisabled" else "getGaussBlurDisabled"
                clazz.method { name = blurMethod }.ignored().hook {
                    replaceToFalse()
                }
                clazz.method { name = "isPanViewBlurDisabled" }.ignored().hook {
                    replaceToFalse()
                }
            }
        }
    }

    private object HookFlavorOneFeature : YukiBaseHooker() {
        override fun onHook() {
            val searchBtnMode =
                prefs(ModulePrefs).getString("set_control_center_search_button_mode", "0")
            var showWattage =
                prefs(ModulePrefs).getBoolean("force_lock_screen_charging_show_wattage", false)
            dataChannel.wait<Boolean>("force_lock_screen_charging_show_wattage") {
                showWattage = it
            }
            val appMediaVolume =
                prefs(ModulePrefs).getBoolean("enable_app_specific_media_volume", false)

            val clazz = "com.oplusos.systemui.common.feature.FlavorOneFeatureOption".toClassOrNull(appClassLoader) ?: return

            clazz.method { name = "isSupportSearch" }.ignored().hook {
                before {
                    when (searchBtnMode) {
                        "1" -> result = true
                        "2" -> result = false
                    }
                }
            }
            if (showWattage) {
                clazz.method { name = "isShowChargingWattage" }.ignored().hook {
                    replaceToTrue()
                }
            }
            if (appMediaVolume) {
                clazz.method { name = "isFlavorOneMultiMediaDevice" }.ignored().hook {
                    replaceToTrue()
                }
            }
        }
    }

    private object HookQSFeatureOption : YukiBaseHooker() {
        override fun onHook() {
            val autoBrightnessMode =
                prefs(ModulePrefs).getString("set_auto_brightness_button_mode", "0")
            val seekbarMode =
                prefs(ModulePrefs).getString("set_control_center_volume_seekbar_mode", "0")

            val clazz = "com.oplusos.systemui.common.feature.QSFeatureOption".toClassOrNull(appClassLoader) ?: return

            clazz.method { name = "getShouldRemoveAutoBrightness" }.ignored().hook {
                before {
                    when (autoBrightnessMode) {
                        "1" -> result = false
                        "2" -> result = true
                    }
                }
            }
            clazz.method { name = "shouldRemoveAutoBrightness" }.ignored().hook {
                before {
                    when (autoBrightnessMode) {
                        "1" -> result = false
                        "2" -> result = true
                    }
                }
            }
            if (seekbarMode != "0") {
                clazz.method { name = "isSupportVolumeSeekBar" }.ignored().hook {
                    before {
                        when (seekbarMode) {
                            "1" -> result = true
                            "2" -> result = false
                        }
                    }
                }
            }
        }
    }

    private object HookVolumeFeatureOption : YukiBaseHooker() {
        override fun onHook() {
            var volumeBlur =
                prefs(ModulePrefs).getInt("custom_volume_dialog_background_transparency", -1)
            dataChannel.wait<Int>("custom_volume_dialog_background_transparency") {
                volumeBlur = it
            }
            if (volumeBlur <= -1) return

            val clazz = "com.oplusos.systemui.common.feature.VolumeFeatureOption".toClassOrNull(appClassLoader) ?: return
            clazz.method { name = "isVolumeBlurDisabled" }.ignored().hook {
                replaceToFalse()
            }
        }
    }
}
