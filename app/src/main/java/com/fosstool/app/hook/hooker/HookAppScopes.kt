package com.fosstool.app.hook.hooker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.constructor
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.ActivityClass
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.LongType
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK
import com.fosstool.app.hook.utils.OplusBuildUtlils
import java.net.Inet4Address
import java.net.Inet6Address
import org.json.JSONObject

object HookBeaconLink : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("remove_beacon_link_time_limit", false)) return
        if (SDK < A13) return
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findMethod {
                matcher {
                    returnType = BooleanType.name
                    usingStrings("beaconlink", "time_limit")
                }
            }.apply {
                checkDataList("HookBeaconLink")
                first().apply {
                    val cls = className.toClass()
                    cls.method {
                        name = methodName
                        returnType = BooleanType
                    }.hook { replaceToTrue() }
                    runCatching {
                        cls.constructor {
                            param(StringClass, LongType)
                        }.hook {
                            before {
                                args(1).set(0L)
                            }
                        }
                    }
                }
            }
        }
    }
}

object HookCalendar : YukiBaseHooker() {
    override fun onHook() {
        val removeHoliday = prefs(ModulePrefs).getBoolean("remove_holiday_page_information_flow", false) ||
            prefs(ModulePrefs).getBoolean("remove_holiday_page_feed", false)
        val removeAlmanac = prefs(ModulePrefs).getBoolean("remove_almanac_page_information_flow", false) ||
            prefs(ModulePrefs).getBoolean("remove_almanac_page_feed", false)
        val removeHoroscope = prefs(ModulePrefs).getBoolean("remove_horoscope_page_information_flow", false) ||
            prefs(ModulePrefs).getBoolean("remove_constellation_page_feed", false)
        if (!removeHoliday && !removeAlmanac && !removeHoroscope) return

        if (removeHoliday) {
            runCatching {
                "com.coloros.calendar.app.specialholiday.SpecialHolidayWebViewDetailViewModel"
                    .toClass().apply {
                        method { name = "buildHolidayH5UrlInner" }.hookAll {
                            after { result = "" }
                        }
                    }
            }
        }
        if (removeAlmanac) {
            runCatching {
                "com.android.calendar.module.subscription.almanac.adapter.AlmanacPagesAdapter"
                    .toClass().apply {
                        method { name = "onCreateViewHolder" }.hookAll {
                            before {
                                for (i in 3 downTo 0) {
                                    val v = runCatching { args(i).any() }.getOrNull()
                                    if (v is Int) {
                                        args(i).set(0)
                                        break
                                    }
                                }
                            }
                        }
                        method { name = "getItemCount" }.hookAll {
                            after { result = 0 }
                        }
                    }
            }
        }
        if (removeHoroscope) {
            runCatching {
                "com.android.calendar.module.subscription.horoscope.HoroscopeFragment"
                    .toClass().apply {
                        method { name = "onViewCreated" }.hookAll {
                            after {
                                runCatching {
                                    val v = instance as? android.view.View
                                        ?: instance?.javaClass?.methods
                                            ?.firstOrNull { it.name == "getView" && it.parameterCount == 0 }
                                            ?.invoke(instance) as? android.view.View
                                    v?.visibility = android.view.View.GONE
                                }
                            }
                        }
                    }
            }
        }
    }
}

object HookEngineerMode : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("unlock_some_hidden_options", false) &&
            !prefs(ModulePrefs).getBoolean("unlock_engineer_mode_hidden_options", false)
        ) return
        val hooked = runCatching {
            "com.oplus.engineermode.impl.SecrecyServiceHelper".toClass().apply {
                method {
                    name = "isSecrecySupported"
                    returnType = BooleanType
                }.hook { replaceToTrue() }
                method {
                    name = "getSecrecyState"
                    returnType = BooleanType
                }.hook { replaceToFalse() }
            }
            true
        }.getOrDefault(false)
        if (hooked) return
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findMethod {
                matcher {
                    returnType = BooleanType.name
                    usingStrings("hidden", "engineer_mode")
                }
            }.apply {
                checkDataList("HookEngineerMode")
                first().apply {
                    className.toClass().method {
                        name = methodName
                        returnType = BooleanType
                    }.hook { replaceToTrue() }
                }
            }
        }
    }
}

object HookEyeProtect : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("enable_eyeprotect_paper_texture_support", false)) return
        if (SDK < A13) return
        val featureMap = mapOf(
            "oplus.software.display.eyeprotect_paper_texture_support" to true,
            "oplus.software.display.smart_color_temperature_rhythm_health_support" to true,
        )
        runCatching {
            "com.oplus.content.OplusFeatureConfigManager".toClass().apply {
                method {
                    name = "hasFeature"
                    param(StringClass)
                    returnType = BooleanType
                }.hook {
                    before {
                        val key = args().first().string()
                        featureMap[key]?.let { result = it }
                    }
                }
            }
        }
    }
}

object HookFileManager : YukiBaseHooker() {
    override fun onHook() {
        val removeSave = prefs(ModulePrefs).getBoolean("remove_word_limit_for_saving_files", false) ||
            prefs(ModulePrefs).getBoolean("remove_file_save_word_limit", false)
        val removeCompress = prefs(ModulePrefs).getBoolean("remove_word_limit_for_compress_files", false) ||
            prefs(ModulePrefs).getBoolean("remove_compress_file_word_limit", false)
        val removeRename = prefs(ModulePrefs).getBoolean("remove_word_limit_for_label_name_files", false) ||
            prefs(ModulePrefs).getBoolean("remove_rename_file_word_limit", false)
        if (!removeSave && !removeCompress && !removeRename) return
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findMethod {
                matcher {
                    paramTypes(StringClass.name)
                    returnType = BooleanType.name
                    usingStrings("invalid", "file_name")
                }
            }.apply {
                checkDataList("HookFileManager")
                first().apply {
                    className.toClass().method {
                        name = methodName
                        param(StringClass)
                        returnType = BooleanType
                    }.hook {
                        before { result = false }
                        after {
                            if (removeSave) hideWelfareView(instance)
                        }
                    }
                }
            }
        }
    }

    private fun hideWelfareView(target: Any?) {
        target ?: return
        runCatching {
            for (f in target.javaClass.declaredFields) {
                if (!android.view.View::class.java.isAssignableFrom(f.type)) continue
                f.isAccessible = true
                val v = f.get(target) as? android.view.View ?: continue
                v.visibility = 8
            }
        }
    }
}

object HookHealth : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("remove_health_root_check_dialog", false) &&
            !prefs(ModulePrefs).getBoolean("remove_health_root_detection_dialog", false)
        ) return
        val hooked = runCatching {
            "com.heytap.health.safety.safetycheck.SafetyCheckManager".toClass().apply {
                method { param(ActivityClass) }.hookAll { intercept() }
            }
            true
        }.getOrDefault(false)
        if (hooked) return
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findMethod {
                matcher {
                    returnType = BooleanType.name
                    usingStrings("root", "magisk")
                }
            }.apply {
                checkDataList("HookHealth")
                first().apply {
                    className.toClass().method {
                        name = methodName
                        returnType = BooleanType
                    }.hook { replaceToFalse() }
                }
            }
        }
    }
}


object HookContactsScope : YukiBaseHooker() {
    override fun onHook() {
        loadHooker(
            com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker(
                mode = com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker.Mode.RM0_Q
            )
        )
    }
}

object HookBluetoothScope : YukiBaseHooker() {
    override fun onHook() {
        loadHooker(
            com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker(
                mode = com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker.Mode.RM0_Q
            )
        )
    }
}

object HookAtlasScope : YukiBaseHooker() {
    override fun onHook() {
        loadHooker(
            com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker(
                mode = com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker.Mode.BOTH
            )
        )
    }
}

object HookAccessoryScope : YukiBaseHooker() {
    override fun onHook() {
        loadHooker(
            com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker(
                mode = com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker.Mode.RM0_T
            )
        )
    }
}

object HookMcs : YukiBaseHooker() {
    override fun onHook() {
        loadHooker(
            com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker(
                mode = com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker.Mode.RM0_T,
                includeRegionDefaults = true,
            )
        )
    }
}

object HookMyDevices : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("force_enable_feiniu_cloud_nas_option", false) &&
            !prefs(ModulePrefs).getBoolean("support_fn_nas", false)
        ) return
        val propKey = "ro.oplus.feiniunas.support"
        listOf(
            "android.os.SystemProperties",
            "com.oplus.wrapper.os.SystemProperties",
        ).forEach { cls ->
            runCatching {
                cls.toClass().apply {
                    method {
                        name = "get"
                        param(StringClass, StringClass)
                        returnType = StringClass
                    }.hook {
                        after {
                            if (args().first().string() == propKey) result = "true"
                        }
                    }
                    method {
                        name = "get"
                        param(StringClass)
                        returnType = StringClass
                    }.hook {
                        after {
                            if (args().first().string() == propKey) result = "true"
                        }
                    }
                    method {
                        name = "getBoolean"
                        param(StringClass, BooleanType)
                        returnType = BooleanType
                    }.hook {
                        before {
                            if (args().first().string() == propKey) resultTrue()
                        }
                    }
                }
            }
        }
    }
}

object HookNfc : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("scan_nfc_tag_auto_click", false) &&
            !prefs(ModulePrefs).getBoolean("scan_nfc_tag_auto_click_button", false)
        ) return
        val action = "com.oplus.nfc.dispatch.TagDetectedNotification.ACTION_PROCESS_TAG"
        val hooked = runCatching {
            "com.oplus.nfc.dispatch.TagDetectedNotification".toClass().apply {
                method { name = "show" }.hookAll {
                    before {
                        val ctx = args().first().cast<Context>() ?: return@before
                        val dispatcherIntent = runCatching {
                            args(1).cast<Intent>()
                        }.getOrNull() ?: return@before
                        val componentType = runCatching {
                            args(2).cast<Int>()
                        }.getOrNull() ?: 0
                        val intent = Intent().apply {
                            setAction(action)
                            putExtra("dispatcherIntent", dispatcherIntent)
                            putExtra("componentType", componentType)
                            setPackage("com.android.nfc")
                        }
                        PendingIntent.getBroadcast(
                            ctx,
                            System.currentTimeMillis().toInt(),
                            intent,
                            201326592,
                        ).send()
                    }
                }
            }
            true
        }.getOrDefault(false)
        if (hooked) return
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findMethod {
                matcher {
                    name = "show"
                    usingStrings("dispatch")
                }
            }.apply {
                checkDataList("HookNfc")
                first().apply {
                    className.toClass().method {
                        name = methodName
                    }.hook {
                        before {
                            val ctx = args().first().cast<Context>() ?: return@before
                            val dispatcherIntent = runCatching {
                                args(1).cast<Intent>()
                            }.getOrNull() ?: return@before
                            val componentType = runCatching {
                                args(2).cast<Int>()
                            }.getOrNull() ?: 0
                            val intent = Intent().apply {
                                setAction(action)
                                putExtra("dispatcherIntent", dispatcherIntent)
                                putExtra("componentType", componentType)
                                setPackage("com.android.nfc")
                            }
                            runCatching {
                                PendingIntent.getBroadcast(
                                    ctx,
                                    System.currentTimeMillis().toInt(),
                                    intent,
                                    201326592,
                                ).send()
                            }
                        }
                    }
                }
            }
        }
    }
}

object HookOShare : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("remove_oshare_close_countdown", false)) return
        fun hookTimeout(className: String): Boolean = runCatching {
            className.toClass().apply {
                method {
                    name = "getSwitchTimeOut"
                    param(ContextClass)
                    returnType = LongType
                }.hook { replaceTo(0L) }
            }
            true
        }.getOrDefault(false)
        if (hookTimeout("com.coloros.oshare.OShareFeatureConfig")) return
        if (hookTimeout("com.coloros.oshare.config.OShareFeatureConfig")) return
        if (hookTimeout("com.oplus.oshare.OShareFeatureConfig")) return
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findClass {
                matcher { usingStrings("OShareFeatureConfig") }
            }.apply {
                checkDataList("HookOShare.class", onlyOne = false)
                for (data in this) {
                    if (hookTimeout(data.name)) return@create
                }
            }
            dexKitBridge.findMethod {
                matcher {
                    name = "getSwitchTimeOut"
                    paramTypes(ContextClass.name)
                    returnType = LongType.name
                }
            }.apply {
                checkDataList("HookOShare.getSwitchTimeOut", onlyOne = false)
                firstOrNull()?.apply {
                    className.toClass().method {
                        name = methodName
                        param(ContextClass)
                        returnType = LongType
                    }.hook { replaceTo(0L) }
                }
            }
        }
    }
}

object HookSecurityPermission : YukiBaseHooker() {
    override fun onHook() {
        val disableIntercept = prefs(ModulePrefs).getBoolean("disable_malicious_app_intercept", false)
        val useOldDialog = prefs(ModulePrefs).getBoolean("app_start_dialog_use_old_version", false) ||
            prefs(ModulePrefs).getBoolean("use_old_version_app_jump_dialog", false)
        val enableAlwaysAllow = prefs(ModulePrefs).getBoolean("enable_always_allow_app_start_dialog", false)
        val autoUnlock = prefs(ModulePrefs).getBoolean("auto_unlock_app_ecm_permission_restrict", false) ||
            prefs(ModulePrefs).getBoolean("auto_unlock_app_permission_management_limit", false)
        if (!disableIntercept && !useOldDialog && !enableAlwaysAllow && !autoUnlock) return
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            if (disableIntercept) {
                dexKitBridge.findMethod {
                    matcher {
                        returnType = BooleanType.name
                        usingStrings("malicious", "intercept")
                    }
                }.apply {
                    checkDataList("HookSecurityPermission.disableIntercept")
                    first().apply {
                        className.toClass().method {
                            name = methodName
                            returnType = BooleanType
                        }.hook { replaceToFalse() }
                    }
                }
            }
            if (useOldDialog) {
                dexKitBridge.findMethod {
                    matcher {
                        returnType = BooleanType.name
                        usingStrings("old_version", "jump_dialog")
                    }
                }.apply {
                    checkDataList("HookSecurityPermission.useOldDialog")
                    first().apply {
                        className.toClass().method {
                            name = methodName
                            returnType = BooleanType
                        }.hook { replaceToTrue() }
                    }
                }
            }
            if (enableAlwaysAllow) {
                dexKitBridge.findMethod {
                    matcher {
                        returnType = BooleanType.name
                        usingStrings("always_allow", "app_start")
                    }
                }.apply {
                    checkDataList("HookSecurityPermission.enableAlwaysAllow")
                    first().apply {
                        className.toClass().method {
                            name = methodName
                            returnType = BooleanType
                        }.hook { replaceToTrue() }
                    }
                }
            }
            if (autoUnlock) {
                dexKitBridge.findMethod {
                    matcher {
                        returnType = BooleanType.name
                        usingStrings("permission_management", "limit")
                    }
                }.apply {
                    checkDataList("HookSecurityPermission.autoUnlock")
                    first().apply {
                        className.toClass().method {
                            name = methodName
                            returnType = BooleanType
                        }.hook { replaceToFalse() }
                    }
                }
            }
        }
    }
}

object HookSmartSidebar : YukiBaseHooker() {
    override fun onHook() {
        loadHooker(
            com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker(
                mode = com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker.Mode.RM0_Q
            )
        )
        val autoHide = prefs(ModulePrefs).getBoolean("force_enable_buoy_automatically_hides", false) ||
            prefs(ModulePrefs).getBoolean("force_enable_sidebar_auto_hide", false)
        val transferStation = prefs(ModulePrefs).getBoolean("unlock_transfer_dock", false) ||
            prefs(ModulePrefs).getBoolean("unlock_transfer_station", false)
        val recentFiles = prefs(ModulePrefs).getBoolean("unlock_recent_files", false)
        val runInBg = prefs(ModulePrefs).getBoolean("enable_run_in_background", false)
        if (!autoHide && !transferStation && !recentFiles && !runInBg) return
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            if (autoHide) {
                dexKitBridge.findMethod {
                    matcher {
                        returnType = BooleanType.name
                        usingStrings("sidebar", "auto_hide")
                    }
                }.apply {
                    checkDataList("HookSmartSidebar.autoHide")
                    first().apply {
                        className.toClass().method {
                            name = methodName
                            returnType = BooleanType
                        }.hook { replaceToTrue() }
                    }
                }
            }
            if (transferStation) {
                dexKitBridge.findMethod {
                    matcher {
                        returnType = BooleanType.name
                        usingStrings("transfer_station", "transfer_dock")
                    }
                }.apply {
                    checkDataList("HookSmartSidebar.transferStation")
                    first().apply {
                        className.toClass().method {
                            name = methodName
                            returnType = BooleanType
                        }.hook { replaceToTrue() }
                    }
                }
            }
            if (recentFiles) {
                dexKitBridge.findMethod {
                    matcher {
                        returnType = BooleanType.name
                        usingStrings("recent_files")
                    }
                }.apply {
                    checkDataList("HookSmartSidebar.recentFiles")
                    first().apply {
                        className.toClass().method {
                            name = methodName
                            returnType = BooleanType
                        }.hook { replaceToTrue() }
                    }
                }
            }
            if (runInBg) {
                dexKitBridge.findMethod {
                    matcher {
                        returnType = BooleanType.name
                        usingStrings("run_in_background", "sidebar")
                    }
                }.apply {
                    checkDataList("HookSmartSidebar.runInBg")
                    first().apply {
                        className.toClass().method {
                            name = methodName
                            returnType = BooleanType
                        }.hook { replaceToTrue() }
                    }
                }
            }
        }
    }
}

object HookSoundRecorder : YukiBaseHooker() {
    override fun onHook() {
        loadHooker(
            com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker(
                mode = com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker.Mode.RM0_T
            )
        )
    }
}

object HookSpeechAssist : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("force_enable_ai_speechassist_call", false) &&
            !prefs(ModulePrefs).getBoolean("force_enable_xiaobu_call", false)
        ) return
        val hooked = runCatching {
            "com.heytap.speechassist.aicall.setting.config.AiCallCommonBean".toClass().apply {
                method {
                    name = "getSupportAiCall"
                    returnType = BooleanType
                }.hook { replaceToTrue() }
                method {
                    name = "getSupportAiCallV2"
                    returnType = BooleanType
                }.hook { replaceToTrue() }
            }
            true
        }.getOrDefault(false)
        if (hooked) return
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findMethod {
                matcher {
                    returnType = BooleanType.name
                    usingStrings("xiaobu", "call")
                }
            }.apply {
                checkDataList("HookSpeechAssist")
                first().apply {
                    className.toClass().method {
                        name = methodName
                        returnType = BooleanType
                    }.hook { replaceToTrue() }
                }
            }
        }
    }
}

object HookTeleService : YukiBaseHooker() {
    override fun onHook() {
        val force5G = prefs(ModulePrefs).getBoolean("force_display_five_g_switch", false) ||
            prefs(ModulePrefs).getBoolean("force_display_5g_switch", false)
        val forceVoLTE = prefs(ModulePrefs).getBoolean("force_display_volte_calls", false) ||
            prefs(ModulePrefs).getBoolean("force_display_volte_hd_call", false)
        val forceNetworkType = prefs(ModulePrefs).getBoolean("force_display_preferred_network_type", false)
        if (!force5G && !forceVoLTE && !forceNetworkType) return

        if (forceVoLTE || forceNetworkType) {
            runCatching {
                "com.android.simsettings.activity.OplusSimInfoActivity".toClass().apply {
                    if (forceVoLTE) {
                        method { name = "changeVolteSwitchConfig"; paramCount = 3 }.hookAll {
                            before {
                                if (!forceVoLTE) return@before
                                val first = runCatching { args(0).any() as? Int }.getOrNull() ?: return@before
                                if (first != 1) return@before
                                for (i in 1..2) {
                                    runCatching { if (args(i).any() is Boolean) { args(i).set(true); return@before } }
                                }
                            }
                        }
                    }
                    if (forceNetworkType) {
                        method { name = "changeNetworkModeConfig"; paramCount = 3 }.hookAll {
                            before {
                                if (!forceNetworkType) return@before
                                val first = runCatching { args(0).any() as? Int }.getOrNull() ?: return@before
                                if (first != 1) return@before
                                for (i in 1..2) {
                                    runCatching { if (args(i).any() is Boolean) { args(i).set(true); return@before } }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

object HookWirelessSettings : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("enable_wifi_details_display_gateway", false) &&
            !prefs(ModulePrefs).getBoolean("enable_wifi_detail_show_gateway", false)
        ) return
        val classes = listOf(
            "com.oplus.wirelesssettings.wifi.detail.WifiAddressController",
            "com.oplus.wirelesssettings.wifi.detail2.WifiAddressController",
        )
        var any = false
        for (cls in classes) {
            any = runCatching {
                cls.toClass().apply {
                    method { name = "updateIpInfo" }.hookAll {
                        after { injectWifiGateway(instance) }
                    }
                }
                true
            }.getOrDefault(false) || any
        }
        if (any) return
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findMethod {
                matcher { name = "updateIpInfo" }
            }.apply {
                checkDataList("HookWirelessSettings", onlyOne = false)
                forEach { data ->
                    runCatching {
                        data.className.toClass().method {
                            name = data.methodName
                        }.hook {
                            after { injectWifiGateway(instance) }
                        }
                    }
                }
            }
        }
    }

    private fun injectWifiGateway(controller: Any?) {
        controller ?: return
        runCatching {
            val fields = controller.javaClass.declaredFields
            val screen = fields.firstOrNull {
                it.type.name.endsWith("PreferenceScreen")
            }?.also { it.isAccessible = true }?.get(controller) ?: return
            val findPref = screen.javaClass.methods.firstOrNull {
                it.name == "findPreference" &&
                    it.parameterTypes.size == 1 &&
                    it.parameterTypes[0] == String::class.java
            } ?: return
            val ctx = fields.firstOrNull {
                Context::class.java.isAssignableFrom(it.type)
            }?.also { it.isAccessible = true }?.get(controller) as? Context
            if (ctx == null) return
            val cm = ctx.getSystemService(ConnectivityManager::class.java) ?: return
            @Suppress("DEPRECATION")
            val wm = ctx.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                ?: return
            val network = runCatching {
                WifiManager::class.java.getMethod("getCurrentNetwork").invoke(wm)
            }.getOrNull() as? android.net.Network ?: return
            val lp = cm.getLinkProperties(network) ?: return
            var v4: String? = null
            var v6: String? = null
            for (route in lp.routes) {
                if (!route.isDefaultRoute) continue
                val gw = route.gateway?.hostAddress ?: continue
                when (route.destination?.address) {
                    is Inet4Address -> v4 = gw
                    is Inet6Address -> v6 = gw
                }
            }
            fun setGw(key: String, gateway: String?) {
                if (gateway.isNullOrEmpty()) return
                val pref = findPref.invoke(screen, key) ?: return
                val isEnabled = runCatching {
                    pref.javaClass.getMethod("isEnabled").invoke(pref) as Boolean
                }.getOrDefault(false)
                if (!isEnabled) return
                val title = runCatching {
                    pref.javaClass.getMethod("getTitle").invoke(pref)?.toString()
                }.getOrNull() ?: return
                runCatching {
                    pref.javaClass.getMethod("setSummary", CharSequence::class.java)
                        .invoke(pref, "$title\n$gateway")
                }
            }
            setGw("current_ipv4_address", v4)
            setGw("current_ipv6_address", v6)
        }
    }
}

object HookMultiApp : YukiBaseHooker() {
    override fun onHook() {
        loadHooker(
            com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker(
                mode = com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker.Mode.RM0_Q
            )
        )
        loadHooker(com.fosstool.app.hook.scope.multiapp.RemoveMultiAppBlacklist)
    }
}

object HookMediaController : YukiBaseHooker() {
    private const val KEY_STATIC_VOICE_PRINT_SHOW = "staticVoicePrintShow"

    override fun onHook() {
        val forceRipple =
            prefs(ModulePrefs).getBoolean("force_enable_media_music_fluid_cloud_ripple", false)
        if (!forceRipple) return
        val osVersionCode = try { OplusBuildUtlils().getOSVersionCode ?: 0 } catch (_: Throwable) { 0 }
        if (osVersionCode < 33) return
        runCatching {
            "com.oplus.pantanal.seedling.util.SeedlingTool".toClass().apply {
                method {
                    name { it.isNotEmpty() }
                    modifiers { isStatic }
                    paramCount(1..8)
                }.hookAll {
                    before {
                        val json = runCatching { args(1).any() as? JSONObject }.getOrNull()
                            ?: run {
                                var found: JSONObject? = null
                                for (i in 0 until 6) {
                                    val v = runCatching { args(i).any() }.getOrNull()
                                    if (v is JSONObject) { found = v; break }
                                }
                                found
                            } ?: return@before
                        if (json.optBoolean(KEY_STATIC_VOICE_PRINT_SHOW, true)) {
                            runCatching { json.put(KEY_STATIC_VOICE_PRINT_SHOW, false) }
                        }
                    }
                }
            }
        }
    }
}

object HookContacts : YukiBaseHooker() {
    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findMethod {
                matcher {
                    returnType = BooleanType.name
                    usingStrings("contacts", "oplus_feature")
                }
            }.apply {
                checkDataList("HookContacts")
                first().apply {
                    className.toClass().method {
                        name = methodName
                        returnType = BooleanType
                    }.hook { replaceToTrue() }
                }
            }
        }
    }
}

object HookBluetooth : YukiBaseHooker() {
    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findMethod {
                matcher {
                    returnType = BooleanType.name
                    usingStrings("bluetooth", "oplus_feature")
                }
            }.apply {
                checkDataList("HookBluetooth")
                first().apply {
                    className.toClass().method {
                        name = methodName
                        returnType = BooleanType
                    }.hook { replaceToTrue() }
                }
            }
        }
    }
}


object HookSau : YukiBaseHooker() {
    override fun onHook() {
        loadHooker(
            com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker(
                mode = com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker.Mode.RM0_T
            )
        )
    }
}

object HookAudioMonitor : YukiBaseHooker() {
    override fun onHook() {
        return
    }
}

object HookAudioEffectCenter : YukiBaseHooker() {
    private const val CLASS_MGR =
        "com.oplus.audio.effectcenter.manager.SpatializerManager"
    private const val CLASS_DEF =
        "com.oplus.audio.effectcenter.manager.SpatializerDefine"

    override fun onHook() {
        val enable = prefs(ModulePrefs)
            .getBoolean("enable_record_calls_on_third_party_apps", false)
        val os = try {
            OplusBuildUtlils(appClassLoader).getOSVersionCode ?: 0
        } catch (_: Throwable) {
            0
        }
        if (!enable || os != 30) return

        runCatching {
            CLASS_MGR.toClass().method { name = "setSpkVolParam" }.hook {
                before {
                    val arg0 = runCatching { args().first().int() }.getOrNull() ?: return@before
                    val mode = runCatching {
                        instance.current().field { name = "mSpatializerMode" }.any()
                            as? java.util.concurrent.atomic.AtomicBoolean
                    }.getOrNull() ?: return@before
                    val spkVol = runCatching {
                        instance.current().field { name = "mSpatializerSpkVol" }.any()
                            as? java.util.concurrent.atomic.AtomicInteger
                    }.getOrNull() ?: return@before
                    val spatDev = runCatching {
                        instance.current().field { name = "mSpatDeviceManager" }.any()
                    }.getOrNull() ?: return@before

                    val device = runCatching {
                        spatDev.current().method {
                            name = "getDeviceForMusicStream"
                            emptyParam()
                        }.invoke<Int>()
                    }.getOrNull() ?: return@before

                    if (arg0 == spkVol.get()) return@before
                    if (device != 2 && mode.get()) return@before

                    val paramIdx = runCatching {
                        CLASS_DEF.toClass().field {
                            name = "PARAM_SET_SPAT_VOLUME_INDEX"
                            modifiers { isStatic }
                        }.get().int()
                    }.getOrNull() ?: return@before

                    runCatching {
                        instance.current().method {
                            name = "setParameterImp"
                            paramCount = 3
                        }.call(paramIdx, arg0, spkVol.get())
                    }
                    resultNull()
                }
            }
        }
    }
}

object HookIncallUI : YukiBaseHooker() {
    override fun onHook() {
        loadHooker(
            com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker(
                mode = com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker.Mode.RM0_Q
            )
        )
    }
}

object HookPhone : YukiBaseHooker() {
    override fun onHook() {
        loadHooker(
            com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker(
                mode = com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker.Mode.BOTH
            )
        )
    }
}
