package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.hasMethod
import com.highcapable.yukihookapi.hook.factory.method
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
        if (SDK >= A14) {
            loadHooker(HookQSFeatureOption)
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

            "com.oplusos.systemui.common.feature.FeatureOption".toClass().apply {
                if (hasMethod { name = "shouldRemoveAutoBrightness" }) {
                    method { name = "shouldRemoveAutoBrightness" }.hook {
                        before {
                            when (autoBrightnessMode) {
                                "1" -> resultFalse()
                                "2" -> resultTrue()
                            }
                        }
                    }
                }
                if (hasMethod { name = "isOriginNotificationBehavior" }) {
                    method { name = "isOriginNotificationBehavior" }.hook {
                        if (notifyImportance) replaceToTrue()
                    }
                }
                if (hasMethod { name = "isVolumeBlurDisabled" }) {
                    method { name = "isVolumeBlurDisabled" }.hook {
                        if (volumeBlur > -1) replaceToFalse()
                    }
                }
                if (hasMethod { name = "isAiSdr2HdrSupport" }) {
                    method { name = "isAiSdr2HdrSupport" }.hook {
                        if (enableBlur) replaceToFalse()
                    }
                }
                if (hasMethod { name = "isOplusVolumeKeyInRight" }) {
                    method { name = "isOplusVolumeKeyInRight" }.hook {
                        before {
                            when (volumePosition) {
                                "1" -> resultFalse()
                                "2" -> resultTrue()
                            }
                        }
                    }
                }
                if (hasMethod { name = "areVolumeAndPowerKeysInRight" }) {
                    method { name = "areVolumeAndPowerKeysInRight" }.hook {
                        before {
                            when (volumePosition) {
                                "1" -> resultFalse()
                                "2" -> resultTrue()
                            }
                        }
                    }
                }
                if (hasMethod { name = "isSupportFullScreenChargeAnim" }) {
                    method { name = "isSupportFullScreenChargeAnim" }.hook {
                        before {
                            when (fullScreenChargeAnim) {
                                "1" -> resultTrue()
                                "2" -> resultFalse()
                            }
                        }
                    }
                }
                if (hasMethod { name = "isSupportShowWattage" }) {
                    method { name = "isSupportShowWattage" }.hook {
                        if (warpCharge == "2" && showWattage) replaceToTrue()
                    }
                }
                if (SDK == A13 && hasMethod { name = "isUseWarpCharge" }) {
                    method { name = "isUseWarpCharge" }.hook {
                        before {
                            when (warpCharge) {
                                "1" -> resultTrue()
                                "2" -> resultFalse()
                            }
                        }
                    }
                }
                if (hasMethod { name = "isSupportMyDevice" }) {
                    method { name = "isSupportMyDevice" }.hook {
                        if (removeMyDevice) replaceToFalse()
                    }
                }
            }
        }
    }

    private object HookStatusBarFeature : YukiBaseHooker() {
        override fun onHook() {
            val hideSignalLabels =
                prefs(ModulePrefs).getBoolean("hide_inactive_signal_labels_gen2x2", false)

            VariousClass(
                "com.oplusos.systemui.statusbar.feature.StatusBarFeatureOption",
                "com.oplusos.systemui.common.feature.StatusBarFeatureOption"
            ).toClass().apply {
                method { name = "isSystemUiExpSignalUi" }.hook {
                    if (hideSignalLabels) replaceToTrue()
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

            VariousClass(
                "com.oplusos.systemui.common.util.NotificationAppFeatureOption",
                "com.oplusos.systemui.common.feature.NotificationFeatureOption"
            ).toClass().apply {
                method {
                    name = if (SDK >= A14) "isOriginNotificationBehavior"
                    else "originNotificationBehavior"
                }.hook {
                    if (notifyImportance) replaceToTrue()
                }

                if (SDK >= A13) method {
                    name = if (SDK >= A14) "isGaussBlurDisabled"
                    else "getGaussBlurDisabled"
                }.hook {
                    if (enableBlur) replaceToFalse()
                }

                if (hasMethod { name = "isPanViewBlurDisabled" }) {
                    method { name = "isPanViewBlurDisabled" }.hook {
                        if (enableBlur) replaceToFalse()
                    }
                }
            }
        }
    }

    private object HookFlavorOneFeature : YukiBaseHooker() {
        override fun onHook() {
            val searchBtnMode =
                prefs(ModulePrefs).getString("set_control_center_search_button_mode", "0")

            "com.oplusos.systemui.common.feature.FlavorOneFeatureOption".toClass().apply {
                if (hasMethod { name = "isSupportSearch" }) {
                    method { name = "isSupportSearch" }.hook {
                        before {
                            when (searchBtnMode) {
                                "1" -> resultTrue()
                                "2" -> resultFalse()
                            }
                        }
                    }
                }
            }
        }
    }

    private object HookQSFeatureOption : YukiBaseHooker() {
        override fun onHook() {
            val autoBrightnessMode =
                prefs(ModulePrefs).getString("set_auto_brightness_button_mode", "0")

            "com.oplusos.systemui.common.feature.QSFeatureOption".toClass().apply {
                method { name = "getShouldRemoveAutoBrightness" }.hook {
                    before {
                        when (autoBrightnessMode) {
                            "1" -> resultFalse()
                            "2" -> resultTrue()
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

            "com.oplusos.systemui.common.feature.VolumeFeatureOption".toClass().apply {
                if (hasMethod { name = "isVolumeBlurDisabled" }) {
                    method { name = "isVolumeBlurDisabled" }.hook {
                        if (volumeBlur > -1) replaceToFalse()
                    }
                }
            }
        }
    }
}
