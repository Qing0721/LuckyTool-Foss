package com.fosstool.app.hook.scope.battery

import android.content.ContentResolver
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.fosstool.app.utils.ModulePrefs
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.android.ContentResolverClass
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge

object BatteryFeatureProvider : YukiBaseHooker() {
    override fun onHook() {
        val openScreenPowerSave = prefs(ModulePrefs).getBoolean("open_screen_power_save", false)
        val openBatteryHealth = prefs(ModulePrefs).getBoolean("open_battery_health", false)
        val stopChargingAt80 = prefs(ModulePrefs).getBoolean("enable_stop_charging_at_80", false)
        val showPhoneUsageScreenTime =
            prefs(ModulePrefs).getBoolean("show_phone_usage_screen_time", false)

        val performanceModeStandbyOptimization =
            prefs(ModulePrefs).getBoolean("performance_mode_and_standby_optimization", false)

        val features = HashMap<String, Any>()

        if (openScreenPowerSave) features["com.oplus.battery.cabc_level_dynamic_enable"] = true
        if (openBatteryHealth) features["os.charge.settings.batterysettings.batteryhealth"] = true
        if (stopChargingAt80) features["com.oplus.battery.one_key_power_save"] = true
        if (showPhoneUsageScreenTime) features["com.oplus.battery.phoneusage.screenon.hide"] = false

        if (performanceModeStandbyOptimization) features["com.android.settings.device_rm"] = true

        if (features.isEmpty()) return

        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findClass {
                matcher {

                    methods {
                        add {

                            paramTypes(ContentResolverClass.name, null)
                        }
                        add {
                            usingStrings("featurename")
                            returnType("android.database.Cursor")
                        }
                    }
                    usingStrings(
                        "content://com.oplus.customize.coreapp.configmanager.configprovider.AppFeatureProvider",
                    )
                }
            }.apply {
                checkDataList("BatteryFeatureProvider")
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
