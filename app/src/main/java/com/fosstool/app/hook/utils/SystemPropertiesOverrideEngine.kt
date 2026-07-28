package com.fosstool.app.hook.utils

import android.provider.Settings
import android.util.ArrayMap
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.type.android.ArrayMapClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.LongType
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK
import com.fosstool.app.utils.getOSVersionCode
import com.fosstool.app.utils.safeOf

private const val LOG_TAG = "SystemPropertiesOverrideEngine"

class SystemPropertiesOverrideEngineHooker(
    private val mode: Mode = Mode.BOTH,
    private val includeRegionDefaults: Boolean = false,
) : YukiBaseHooker() {

    enum class Mode {
        RM0_Q,
        RM0_T,
        BOTH,
    }

    private data class Rule(
        val prefKey: String,
        val propKey: String,
        val value: Any,
        val osMin: Int = 0,
        val sdkMin: Int = 0,
        val sdkMax: Int = 0,
        val osEq: Int? = null,
        val sdkEq: Int? = null,

        val extraCondition: (() -> Boolean)? = null,
    )

    private data class MultiRule(
        val prefKey: String,
        val pairs: List<Pair<String, Any>>,
        val osMin: Int = 0,
        val sdkMin: Int = 0,
        val sdkMax: Int = 0,
        val osEq: Int? = null,
        val sdkEq: Int? = null,
    )

    private data class StringPrefRule(
        val prefKey: String,
        val propKey: String,
        val mappings: List<Pair<String, Any>>,
        val default: String = "0",
        val osMin: Int = 0,
        val sdkMin: Int = 0,
        val osEq: Int? = null,
    )

    private data class StringPrefMultiRule(
        val prefKey: String,
        val mappings: List<Pair<String, List<Pair<String, Any>>>>,
        val default: String = "0",
        val osMin: Int = 0,
        val sdkMin: Int = 0,
        val osEq: Int? = null,
    )

    private data class IntPrefRule(
        val prefKey: String,
        val propKey: String,
        val value: Any,
        val defaultValue: Int = -1,
        val condition: (Int) -> Boolean,
        val osMin: Int = 0,
        val sdkMin: Int = 0,
    )

    private val rm0QRules: List<Rule> = listOf(
        Rule("force_enable_multi_window_mode", "oplus.software.support.zoom.multi_mode", true),
        Rule("disable_long_press_home_key_start_speech_asssist", "oplus.software.speech_assist_for_breeno", false),
        Rule("remove_multi_app_created_num_limit_for_users", "oplus.software.multiapp_max_open_number_limited", false),
        Rule("force_enable_iphone_shared_support", "oplus.software.radio.hfp_comm_shared_support", true),
        Rule("force_enable_systemui_blur_feature", "oplus.software.display.osie_aisdr2hdr_support", false),
        Rule("enable_super_volume_mode", "oplus.software.audio.super_volume", true, sdkMin = 33),
        Rule("enable_super_volume_mode_for_calls", "oplus.software.audio.super_volume_call_earpiece", true, osMin = 27),
        Rule("enable_app_specific_media_volume", "oplus.software.multi_app.volume.adjust.support", true, osMin = 27),
        Rule("disable_preload_splash", "oplus.software.wms.disable_preload_splash", true, sdkMin = 33),

        Rule(
            "enable_screen_color_temperature_rgb_ball",
            "oplus.software.display.rgb_ball_support",
            true,
            osMin = 27,
            extraCondition = {
                Settings.System.getUriFor("oplus_settings_switch_color_mode") != null
            },
        ),
        Rule(
            "enable_screen_color_temperature_rgb_space",
            "oplus.software.display.color_space_support",
            true,
            osMin = 30,
            extraCondition = {
                Settings.System.getUriFor("color_space_adjustment") != null
            },
        ),
        Rule("enable_dedicated_ram_for_games", "oplus.software.game_bounce_support", true),
        Rule("enable_smart_switching_screen_resolutions", "oplus.software.display.resolution_switch_disableauto_support", false),
        Rule("enable_video_memc_frame_insertion", "oplus.software.display.pixelworks_enable", true),
        Rule("force_enable_reduce_white_point_value", "oplus.software.display.reduce_white_point", true),
        Rule("enable_swipe_up_navigation_gesture", "com.android.systemui.keep_swipup_gestures", true),
        Rule("enable_recent_task_pin_capsule", "oplus.software.systemui.pin_task", true, osMin = 37),
        Rule("remove_verification_code_floating_window", "oplus.software.inputmethod.verify_code_enable", false),
        Rule("force_enable_aon_gestures", "oplus.software.aon_enable", true),
        Rule("enable_volume_key_control_flashlight", "oplus.software.powerkey_disbale_turnoff_torch", false),
        Rule("unlock_default_desktop_limit", "oplus.software.defaultapp.remove_force_launcher", true),
        Rule("enable_clear_voice", "oplus.hardware.audio.voice_isolation_support", true, sdkMin = 34),
        Rule("enable_sound_sealed_call", "oplus.hardware.audio.dipole_speaker_support", true, sdkMin = 34),
        Rule("force_enable_buoy_automatically_hides", "oplus.software.smart_sidebar", true, sdkEq = 31),
        Rule("enable_show_never_timeout", "oplus.software.screen_off_never_support", true, sdkMax = 35),
        Rule("enable_extra_brightness", "oplus.software.display.sec_max_brightness_rm", true, osMin = 30),
        Rule("enable_game_acceleration", "oplus.software.game.cold.start.speedup.enable", true, osMin = 30),
        Rule("enable_game_architecture_display", "oplus.software.game.hummingbird", true, osMin = 34),
        Rule("enable_eyeprotect_paper_texture_support", "oplus.software.display.smart_color_temperature_rhythm_health_support", true, osMin = 33),
        Rule("enable_run_in_background", "oplus.software.background_stream_tileservice_enabled", true, osMin = 27),
    )

    private val rm0TRules: List<Rule> = listOf(
        Rule("force_enable_systemui_blur_feature", "ro.surface_flinger.supports_background_blur", true),
        Rule("enable_holographic_audio", "ro.oplus.audio.support.meta_audio", 1),
        Rule("enable_lowest_allowed_brightness", "ro.oplus.display.brightness.min_settings.rm", "1,2,15,4.0,0"),
        Rule("enable_mariana_npu_introduction_page", "ro.vendor.oplus.camera.isSupportExplorer", true),
        Rule("enable_hasselblad_camera_introduction_page", "ro.vendor.oplus.camera.isHasselbladCamera", true),
        Rule("force_display_five_g_switch", "ro.oplus.radio.hide_nr_switch", -1),
        Rule("enable_record_calls_on_third_party_apps", "ro.oplus.audio.voip_record_white_app_support", true, osEq = 30),
        Rule("disable_dm_verity_verification", "ro.boot.veritymode", "enforcing"),

        Rule("force_enable_feiniu_cloud_nas_option", "ro.oplus.feiniunas.support", true),
        Rule("enable_video_memc_frame_insertion", "ro.oplus.display.memc_video_refreshrate", true),
    )

    private val rm0QMultiRules: List<MultiRule> = listOf(
        MultiRule("force_enable_iphone_shared_support", listOf(
            "oplus.software.radio.hfp_comm_shared_support" to true,
            "oplus.software.radio.hfp_sms_shared_not_support" to false,
            "oplus.software.radio.hfp_call_shared_not_support" to false,
        )),
        MultiRule("enable_super_volume_mode", listOf(
            "oplus.software.audio.super_volume" to true,
            "oplus.software.audio.super_volume_3x" to true,
        ), sdkMin = 33),
        MultiRule("enable_super_volume_mode_for_calls", listOf(
            "oplus.software.audio.super_volume_call_earpiece" to true,
            "oplus.software.audio.super_volume_call_earpiece_disable" to false,
        ), osMin = 27),
        MultiRule("enable_video_memc_frame_insertion", listOf(
            "oplus.software.display.pixelworks_enable" to true,
            "oplus.software.display.iris_enable" to true,
            "oplus.software.display.memc_enable" to true,
            "oplus.software.display.game.memc_enable" to true,
        )),
        MultiRule("force_enable_aon_gestures", listOf(
            "oplus.software.aon_enable" to true,
            "oplus.software.aon_gestureui_enable" to true,
        )),
        MultiRule("enable_volume_key_control_flashlight", listOf(
            "oplus.software.powerkey_disbale_turnoff_torch" to false,
            "oplus.software.key_quickoperate_torch" to true,
        )),
        MultiRule("enable_eyeprotect_paper_texture_support", listOf(
            "oplus.software.display.smart_color_temperature_rhythm_health_support" to true,
            "oplus.software.display.eyeprotect_paper_texture_support" to true,
        ), osMin = 33),
    )

    private val rm0TMultiRules: List<MultiRule> = listOf(
        MultiRule("force_enable_systemui_blur_feature", listOf(
            "ro.surface_flinger.supports_background_blur" to true,
            "persist.sys.sf.disable_blurs" to false,
        )),
        MultiRule("enable_holographic_audio", listOf(
            "ro.oplus.audio.support.meta_audio_speaker" to 1,
            "ro.oplus.audio.support.meta_suspend_effect" to 1,
        ), osMin = 31),
        MultiRule("disable_dm_verity_verification", listOf(
            "ro.boot.veritymode" to "enforcing",
            "ro.boot.vbmeta.device_state" to "locked",
        )),

        MultiRule("enable_video_memc_frame_insertion", listOf(
            "ro.oplus.display.memc_video_refreshrate" to true,
            "vendor.display.show_memc_tomast" to true,
        )),
        MultiRule("force_enable_32_bit_support", listOf(
            "persist.sys.oplus_support_app32_status" to "1",
            "ro.vendor.oplus.app32_boost_support" to "1",
        )),
    )

    private val rm0QStringPrefRules: List<StringPrefRule> = listOf(
        StringPrefRule(
            prefKey = "set_multi_app_support_mode",
            propKey = "oplus.software.multiapp_support_rlm",
            mappings = listOf("1" to false, "2" to true),
        ),
    )

    private val rm0TStringPrefRules: List<StringPrefRule> = listOf(
        StringPrefRule(
            prefKey = "set_volume_bar_display_position",
            propKey = "persist.oplus.software.audio.right_volume_key",
            mappings = listOf("1" to false, "2" to true),
        ),
        StringPrefRule(
            prefKey = "customized_gaussian_blur_effect_level",
            propKey = "ro.oplus.gaussianlevel",
            mappings = listOf("0" to 0, "1" to 1, "2" to 2, "3" to 3),
        ),
    )

    private val rm0TStringPrefMultiRules: List<StringPrefMultiRule> = listOf(
        StringPrefMultiRule(
            prefKey = "set_ltpo_refresh_rate_mode",
            mappings = listOf(
                "1" to listOf("persist.oplus.display.vrr" to "1", "persist.oplus.display.vrr.adfr" to "2"),
                "2" to listOf("persist.oplus.display.vrr" to "0", "persist.oplus.display.vrr.adfr" to "0"),
            ),
        ),
    )

    private val rm0TIntPrefRules: List<IntPrefRule> = listOf(
        IntPrefRule(
            prefKey = "custom_volume_dialog_background_transparency",
            propKey = "ro.oplus.display.disable.volume_blur",
            value = false,
            defaultValue = -1,
            condition = { it > -1 },
        ),
    )

    override fun onHook() {
        val loadQ = mode == Mode.RM0_Q || mode == Mode.BOTH
        val loadT = mode == Mode.RM0_T || mode == Mode.BOTH
        if (loadQ) {
            val featureMap = buildFeatureMap()
            if (featureMap.isNotEmpty()) hookFeatureConfig(featureMap)
        }
        if (loadT) {
            val propMap = buildPropMap()
            if (propMap.isNotEmpty()) hookSystemProperties(propMap)
        }
        if (packageName == "android" && safeOf(false) {
                prefs(ModulePrefs).getBoolean("remove_gms_usage_restrictions", false)
            }) {
            hookRemoveGmsSystemConfigFeatures()
        }
    }

    private fun hookRemoveGmsSystemConfigFeatures() {
        val removeKeys = listOf(
            "cn.google.services",
            "com.google.android.feature.services_updater",
        )
        val cls = "com.android.server.SystemConfig".toClassOrNull()
        if (cls == null) {
            YLog.error("$LOG_TAG: com.android.server.SystemConfig not found in $packageName")
            return
        }
        cls.method {

            name = "getAvailableFeatures"
            returnType = ArrayMapClass
        }.ignored().hookAll {
            after {
                val map = result as? ArrayMap<*, *> ?: return@after
                @Suppress("UNCHECKED_CAST")
                (map as ArrayMap<Any?, Any?>).removeAll(removeKeys)
            }
        }
    }

    private fun hookFeatureConfig(featureMap: Map<String, Boolean>) {
        val manager = "com.oplus.content.OplusFeatureConfigManager".toClassOrNull()
        if (manager == null) {
            YLog.error("$LOG_TAG: com.oplus.content.OplusFeatureConfigManager not found in $packageName")
        } else {
            manager.method {
                name = "hasFeature"
                param(StringClass)
                returnType = BooleanType
            }.ignored().hook {
                before {
                    val key = args().first().string()
                    if (key.isNotEmpty()) {
                        featureMap[key]?.let { result = it }
                    }
                }
            }
        }

        if (packageName != "android") return
        val service = "com.android.server.content.OplusFeatureConfigManagerService".toClassOrNull()
        if (service == null) {
            YLog.error("$LOG_TAG: OplusFeatureConfigManagerService not found in android")
            return
        }
        service.method {
            name = "hasFeature"
            param(StringClass)
            returnType = BooleanType
        }.ignored().hook {
            before {
                val key = args().first().string()
                if (key.isNotEmpty()) {
                    featureMap[key]?.let { result = it }
                }
            }
        }

        service.method {
            name = "hasFeatureMap"
            param(StringClass, IntType)
            returnType = BooleanType
        }.ignored().hook {
            before {
                val key = args().first().string()
                if (key.isNotEmpty()) {
                    featureMap[key]?.let { result = it }
                }
            }
        }
    }

    private fun hookSystemProperties(propMap: Map<String, Any>) {
        val classes = listOf(
            "android.os.SystemProperties",
            "com.oplus.wrapper.os.SystemProperties",
        )
        for (clsName in classes) {
            val cls = clsName.toClassOrNull()
            if (cls == null) {
                YLog.error("$LOG_TAG: $clsName not found in $packageName")
                continue
            }
            cls.method {
                name = "get"
                returnType = StringClass
            }.ignored().hookAll {
                before {
                    val key = args.getOrNull(0) as? String ?: return@before
                    if (key.isEmpty()) return@before
                    val v = propMap[key] ?: return@before

                    result = when (v) {
                        is Boolean -> v.toString()
                        is String -> v
                        else -> v.toString()
                    }
                }
            }
            cls.method {
                name = "getBoolean"
                returnType = BooleanType
            }.ignored().hookAll {
                before {
                    val key = args.getOrNull(0) as? String ?: return@before
                    if (key.isEmpty()) return@before
                    val v = propMap[key] ?: return@before
                    when (v) {
                        is Boolean -> result = v
                        "1", "true" -> result = true
                        "0", "false" -> result = false
                    }
                }
            }
            cls.method {
                name = "getInt"
                returnType = IntType
            }.ignored().hookAll {
                before {
                    val key = args.getOrNull(0) as? String ?: return@before
                    if (key.isEmpty()) return@before
                    val v = propMap[key] ?: return@before
                    when (v) {
                        is Int -> result = v
                        is Long -> result = v.toInt()
                    }
                }
            }
            cls.method {
                name = "getLong"
                returnType = LongType
            }.ignored().hookAll {
                before {
                    val key = args.getOrNull(0) as? String ?: return@before
                    if (key.isEmpty()) return@before
                    val v = propMap[key] ?: return@before
                    when (v) {
                        is Long -> result = v
                        is Int -> result = v.toLong()
                    }
                }
            }
        }
    }

    private fun buildFeatureMap(): Map<String, Boolean> {
        val map = ArrayMap<String, Boolean>()
        val os = getOSVersionCode
        val sdk = SDK
        fun enabled(key: String): Boolean = safeOf(false) {
            prefs(ModulePrefs).getBoolean(key, false)
        }
        fun stringPref(key: String, default: String = ""): String = safeOf(default) {
            prefs(ModulePrefs).getString(key, default) ?: default
        }
        fun applyRule(r: Rule) {
            if (!enabled(r.prefKey)) return
            if (r.osMin > 0 && os < r.osMin) return
            if (r.sdkMin > 0 && sdk < r.sdkMin) return
            if (r.sdkMax > 0 && sdk >= r.sdkMax) return
            if (r.osEq != null && os != r.osEq) return
            if (r.sdkEq != null && sdk != r.sdkEq) return
            if (r.extraCondition?.let { safeOf(false) { it() } } == false) return
            val b = r.value as? Boolean ?: return
            map[r.propKey] = b
        }
        fun applyMultiRule(mr: MultiRule) {
            if (!enabled(mr.prefKey)) return
            if (mr.osMin > 0 && os < mr.osMin) return
            if (mr.sdkMin > 0 && sdk < mr.sdkMin) return
            if (mr.sdkMax > 0 && sdk >= mr.sdkMax) return
            if (mr.osEq != null && os != mr.osEq) return
            if (mr.sdkEq != null && sdk != mr.sdkEq) return
            for ((k, v) in mr.pairs) {
                if (v is Boolean) map[k] = v
            }
        }
        for (r in rm0QRules) applyRule(r)
        for (mr in rm0QMultiRules) applyMultiRule(mr)
        for (sr in rm0QStringPrefRules) {
            if (sr.osMin > 0 && os < sr.osMin) continue
            if (sr.sdkMin > 0 && sdk < sr.sdkMin) continue
            if (sr.osEq != null && os != sr.osEq) continue
            val strVal = stringPref(sr.prefKey, sr.default)
            for ((strKey, value) in sr.mappings) {
                if (strVal == strKey && value is Boolean) {
                    map[sr.propKey] = value
                    break
                }
            }
        }
        return map
    }

    private fun buildPropMap(): Map<String, Any> {
        val map = ArrayMap<String, Any>()
        val os = getOSVersionCode
        val sdk = SDK
        fun enabled(key: String): Boolean = safeOf(false) {
            prefs(ModulePrefs).getBoolean(key, false)
        }
        fun stringPref(key: String, default: String = ""): String = safeOf(default) {
            prefs(ModulePrefs).getString(key, default) ?: default
        }
        fun intPref(key: String, default: Int = -1): Int = safeOf(default) {
            prefs(ModulePrefs).getInt(key, default)
        }
        fun applyRule(r: Rule) {
            if (!enabled(r.prefKey)) return
            if (r.osMin > 0 && os < r.osMin) return
            if (r.sdkMin > 0 && sdk < r.sdkMin) return
            if (r.sdkMax > 0 && sdk >= r.sdkMax) return
            if (r.osEq != null && os != r.osEq) return
            if (r.sdkEq != null && sdk != r.sdkEq) return
            if (r.extraCondition?.let { safeOf(false) { it() } } == false) return
            map[r.propKey] = r.value
        }
        fun applyMultiRule(mr: MultiRule) {
            if (!enabled(mr.prefKey)) return
            if (mr.osMin > 0 && os < mr.osMin) return
            if (mr.sdkMin > 0 && sdk < mr.sdkMin) return
            if (mr.sdkMax > 0 && sdk >= mr.sdkMax) return
            if (mr.osEq != null && os != mr.osEq) return
            if (mr.sdkEq != null && sdk != mr.sdkEq) return
            for ((k, v) in mr.pairs) map[k] = v
        }
        for (r in rm0TRules) applyRule(r)
        for (mr in rm0TMultiRules) applyMultiRule(mr)
        for (sr in rm0TStringPrefRules) {
            if (sr.osMin > 0 && os < sr.osMin) continue
            if (sr.sdkMin > 0 && sdk < sr.sdkMin) continue
            if (sr.osEq != null && os != sr.osEq) continue
            val strVal = stringPref(sr.prefKey, sr.default)
            for ((strKey, value) in sr.mappings) {
                if (strVal == strKey) {
                    map[sr.propKey] = value
                    break
                }
            }
        }
        for (smr in rm0TStringPrefMultiRules) {
            if (smr.osMin > 0 && os < smr.osMin) continue
            if (smr.sdkMin > 0 && sdk < smr.sdkMin) continue
            if (smr.osEq != null && os != smr.osEq) continue
            val strVal = stringPref(smr.prefKey, smr.default)
            for ((strKey, pairs) in smr.mappings) {
                if (strVal == strKey) {
                    for ((k, v) in pairs) map[k] = v
                    break
                }
            }
        }
        for (ir in rm0TIntPrefRules) {
            if (ir.osMin > 0 && os < ir.osMin) continue
            if (ir.sdkMin > 0 && sdk < ir.sdkMin) continue
            val intVal = intPref(ir.prefKey, ir.defaultValue)
            if (ir.condition(intVal)) {
                map[ir.propKey] = ir.value
            }
        }
        if (enabled("remove_gms_usage_restrictions")) {
            val host2 = stringPref("custom_remote_provisioning_hostname", "")
            if (host2.isNotEmpty()) map["remote_provisioning.hostname"] = host2
        }

        if (includeRegionDefaults || packageName == "com.heytap.mcs") {
            val region = stringPref("custom_system_message_region_defaults", "")
            if (region.isNotEmpty()) map["ro.vendor.oplus.regionmark"] = region
        }
        return map
    }
}
