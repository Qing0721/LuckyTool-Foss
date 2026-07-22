package com.fosstool.app.hook.scope.battery

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.ContentResolverClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.ListClass
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.ModulePrefs

object BatteryFeatureProvider : YukiBaseHooker() {
    override fun onHook() {
        val openScreenPowerSave = prefs(ModulePrefs).getBoolean("open_screen_power_save", false)
        val openBatteryHealth = prefs(ModulePrefs).getBoolean("open_battery_health", false)
        val performanceModeStandbyOptimization =
            prefs(ModulePrefs).getBoolean("performance_mode_and_standby_optimization", false)
        val openBatteryOptimize = false
        val stopChargingAt80 = prefs(ModulePrefs).getBoolean("enable_stop_charging_at_80", false)
        val showPhoneUsageScreenTime = prefs(ModulePrefs).getBoolean("show_phone_usage_screen_time", false)

        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findClass {
                matcher {
                    usingStrings("AppFeatureProviderUtils")
                    methods {
                        add {
                            paramTypes(ContentResolverClass.name, StringClass.name)
                            returnType(BooleanType.name)
                        }
                        add {
                            paramTypes(ContentResolverClass.name, StringClass.name, IntType.name)
                            returnType(IntType.name)
                        }
                        add {
                            paramTypes(
                                ContentResolverClass.name, StringClass.name, BooleanType.name
                            )
                            returnType(BooleanType.name)
                        }
                        add {
                            paramTypes(ContentResolverClass.name, StringClass.name)
                            returnType(ListClass.name)
                        }
                    }
                }
            }.apply {
                checkDataList("BatteryFeatureProvider")
                val member = first()
                member.name.toClass().apply {
                    method {
                        param(ContentResolverClass, StringClass)
                        returnType = BooleanType
                    }.hook {
                        before {
                            when (args(1).cast<String>()) {
                                "com.oplus.battery.cabc_level_dynamic_enable" -> if (openScreenPowerSave) resultTrue()
                                "os.charge.settings.batterysettings.batteryhealth" -> if (openBatteryHealth) resultTrue()
                                "com.oplus.battery.life.mode.notificate" -> if (openBatteryOptimize) resultTrue()
                                "com.android.settings.device_rm" -> if (performanceModeStandbyOptimization) resultTrue()
                                "com.oplus.battery.one_key_power_save" -> if (stopChargingAt80) resultTrue()
                                "com.oplus.battery.phoneusage.screenon.hide" -> if (showPhoneUsageScreenTime) resultFalse()
                            }
                        }
                    }
                    method {
                        param(ContentResolverClass, StringClass, IntType)
                        returnType = IntType
                    }.hook {
                        before {
                            val array = arrayOf(args(1).cast<String>(), args(2).cast<Int>())
                            if (array[0] == "com.oplus.battery.life.mode.notificate" && array[1] == 0) {
                                if (openBatteryOptimize) result = 1
                            }
                        }
                    }
                    method {
                        param(ContentResolverClass, StringClass, BooleanType)
                        returnType = BooleanType
                    }.hook {
                        before {
                            val array = arrayOf(args(1).cast<String>(), args(2).cast<Boolean>())
                            when (array[0]) {
                                "com.oplus.battery.disable_deep_sleep" -> resultFalse()
                            }
                        }
                    }
                }
            }
        }

    }
}
