package com.fosstool.app.hook.scope.settings

import android.content.ContentResolver
import android.content.pm.ApplicationInfo
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK
import com.fosstool.app.utils.getOSVersionCode
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge

object HookSettingsFeature : YukiBaseHooker() {
    override fun onHook() {

        loadHooker(HookAppFeatureProvider)

        if (SDK < A13) loadHooker(HookExpUst)
    }

    private object HookExpUst : YukiBaseHooker() {
        override fun onHook() {
            val neverTimeout = prefs(ModulePrefs).getBoolean("enable_show_never_timeout", false)

            DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
                dexKitBridge.findClass {
                    matcher {
                        methods {
                            add { returnType(String::class.java.name) }
                            add { returnType(Boolean::class.javaPrimitiveType!!.name) }
                            add { returnType(ApplicationInfo::class.java.name) }
                            add { paramTypes(String::class.java.name) }
                            add { paramTypes(Int::class.javaPrimitiveType!!.name) }
                            add {
                                paramTypes(
                                    Int::class.javaPrimitiveType!!.name,
                                    String::class.java.name,
                                )
                            }
                            add { paramTypes(String::class.java.name) }
                            add {
                                paramTypes(String::class.java.name, String::class.java.name)
                            }
                        }
                        usingStrings("screen_off_timeout")
                    }
                }.apply {
                    checkDataList("HookExpUst")
                    val clazz = (firstOrNullSafe()?.name ?: return@apply).toClassOrNull(appClassLoader) ?: return@apply
                    clazz.declaredMethods
                        .filter {
                            it.parameterCount == 1 &&
                                it.parameterTypes[0] == Int::class.javaPrimitiveType &&
                                (it.returnType == Boolean::class.javaPrimitiveType ||
                                    it.returnType == java.lang.Boolean::class.java)
                        }
                        .forEach { m ->
                            runCatching {
                                XposedBridge.hookMethod(m, object : XC_MethodHook() {
                                    override fun beforeHookedMethod(param: MethodHookParam) {
                                        when (param.args.getOrNull(0) as? Int) {
                                            11 -> if (SDK < A13 && neverTimeout) param.result = true
                                        }
                                    }
                                })
                            }
                        }
                }
            }
        }
    }

    private object HookAppFeatureProvider : YukiBaseHooker() {
        override fun onHook() {

            val features = HashMap<String, Any>()

            if (prefs(ModulePrefs).getBoolean("remove_statusbar_devmode", false)) {
                features["com.android.systemui.send_developer_mode_notification"] = false
            }
            when (prefs(ModulePrefs).getString("set_auto_brightness_button_mode", "0")) {
                "1" -> features["com.android.systemui.remove_auto_brightness"] = false
                "2" -> features["com.android.systemui.remove_auto_brightness"] = true
            }
            if (prefs(ModulePrefs).getBoolean("enable_notification_importance_classification", false)) {
                features["com.android.systemui.origin_notification_behavior"] = true
            }
            when (prefs(ModulePrefs).getString("set_volume_bar_display_position", "0")) {
                "1" -> features["com.android.systemui.volume_and_power_key_in_right"] = false
                "2" -> features["com.android.systemui.volume_and_power_key_in_right"] = true
            }
            when (prefs(ModulePrefs).getString("set_full_screen_charging_animation_mode", "0")) {
                "1" -> features["com.android.systemui.support_fullscreen_charge_anim"] = true
                "2" -> features["com.android.systemui.support_fullscreen_charge_anim"] = false
            }
            if (prefs(ModulePrefs).getInt("custom_volume_dialog_background_transparency", -1) > -1) {
                features["com.android.systemui.disable_volume_blur"] = false
            }
            if (prefs(ModulePrefs).getBoolean("force_enable_systemui_blur_feature", false)) {
                features["com.android.systemui.gauss_blur_disabled"] = false
                features["com.android.systemui.pan_view_gauss_blur_disabled"] = false
            }

            if (prefs(ModulePrefs).getBoolean("disable_cn_special_edition_setting", false)) {
                features["com.android.settings.cn_version"] = false
            }
            if (prefs(ModulePrefs).getBoolean("enable_show_never_timeout", false)) {
                features["com.android.settings.show_never_timeout"] = true
            }
            when (prefs(ModulePrefs).getString("set_processor_click_page", "0")) {
                "1" -> {
                    features["com.android.settings.processor_detail"] = true
                    features["com.android.settings.processor_detail_gen2"] = false
                }
                "2" -> {
                    features["com.android.settings.processor_detail"] = true
                    features["com.android.settings.processor_detail_gen2"] = true
                }
            }
            if (prefs(ModulePrefs).getBoolean("force_display_process_management", false)) {
                features["com.android.settings.ultimate_cleanup"] = true
            }
            if (prefs(ModulePrefs).getBoolean("screen_physics_size_shown_cm", false)) {
                features["com.android.settings.screen_physics_size_cm"] = true
            }
            if (prefs(ModulePrefs).getBoolean("disable_device_admin_verification_dialog", false)) {
                features["com.android.settings.verification_dialog.disable"] = true
            }
            if (prefs(ModulePrefs).getBoolean("enable_touch_membrane_protector_mode", false)) {
                features["feature.super_settings_smart_touch.support"] = true
            }

            if (prefs(ModulePrefs).getBoolean("disable_otg_auto_off", false) && getOSVersionCode >= 30) {
                features["com.android.systemui.otg_auto_close_alarm_disable"] = true
            }
            if (prefs(ModulePrefs).getBoolean("enable_swipe_up_navigation_gesture", false)) {
                features["com.android.systemui.keep_swipup_gestures"] = true
            }
            if (prefs(ModulePrefs).getBoolean("open_screen_power_save", false)) {
                features["com.oplus.battery.cabc_level_dynamic_enable"] = true
            }
            if (prefs(ModulePrefs).getBoolean("open_battery_health", false)) {
                features["os.charge.settings.batterysettings.batteryhealth"] = true
            }
            if (prefs(ModulePrefs).getBoolean("enable_stop_charging_at_80", false)) {
                features["com.oplus.battery.one_key_power_save"] = true
            }
            if (prefs(ModulePrefs).getBoolean("show_phone_usage_screen_time", false)) {
                features["com.oplus.battery.phoneusage.screenon.hide"] = false
            }
            if (prefs(ModulePrefs).getBoolean("allow_app_names_display_multiple_lines", false)) {
                features["com.android.launcher.APP_NAME_SHOW_IN_TWO_LINES"] = true
            }

            if (features.isEmpty()) return

            DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
                dexKitBridge.findClass {
                    matcher {

                        methods {
                            add { paramTypes(ContentResolver::class.java.name, null) }
                            add {
                                usingStrings("featurename")
                                returnType("android.database.Cursor")
                            }
                        }
                        usingStrings("content://com.oplus.customize.coreapp.configmanager.configprovider.AppFeatureProvider")
                    }
                }.apply {

                    checkDataList("AppFeatureProviderUtils")
                    val clazz = (firstOrNullSafe()?.name ?: return@apply).toClassOrNull(appClassLoader) ?: return@apply
                    hookFeatureQueries(clazz, features)
                }
            }
        }

        private fun hookFeatureQueries(clazz: Class<*>, features: Map<String, Any>) {
            clazz.declaredMethods.forEach { m ->
                val p = m.parameterTypes
                if (p.isEmpty() || !ContentResolver::class.java.isAssignableFrom(p[0])) return@forEach
                if (m.returnType != Boolean::class.javaPrimitiveType &&
                    m.returnType != java.lang.Boolean::class.java
                ) return@forEach
                val keyIndex = when {
                    p.size == 2 && p[1] == String::class.java -> 1
                    p.size == 3 && p[1] == String::class.java &&
                        (p[2] == Boolean::class.javaPrimitiveType ||
                            p[2] == java.lang.Boolean::class.java) -> 1
                    p.size == 3 && p[2] == String::class.java -> 2
                    else -> return@forEach
                }
                runCatching {
                    XposedBridge.hookMethod(m, object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            val key = param.args.getOrNull(keyIndex) as? String ?: return
                            if (key.isBlank()) return
                            val value = features[key] ?: return
                            param.result = value
                        }
                    })
                }
            }
        }
    }
}
