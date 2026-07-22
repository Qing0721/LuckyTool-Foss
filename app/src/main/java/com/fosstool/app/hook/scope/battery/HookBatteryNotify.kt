package com.fosstool.app.hook.scope.battery

import android.app.NotificationManager
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.android.HandlerClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.ModulePrefs

object HookBatteryNotify : YukiBaseHooker() {
    override fun onHook() {
        val highPerformance =
            prefs(ModulePrefs).getBoolean("remove_high_performance_mode_notifications", false)
        val highBatteryConsumption =
            prefs(ModulePrefs).getBoolean("remove_app_high_battery_consumption_warning", false)

        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findClass {
                matcher {
                    fields {
                        addForType(ContextClass.name)
                        addForType(HandlerClass.name)
                        addForType(NotificationManager::class.java.name)
                    }
                    methods {
                        add {
                            paramTypes(StringClass.name, BooleanType.name)
                            returnType(UnitType.name)
                        }
                    }
                    usingStrings("NotifyUtil")
                }
            }.apply {
                checkDataList("HookBatteryNotify NotifyUtil")
                val clsName = first().name

                if (highPerformance) dexKitBridge.findMethod {
                    searchPackages(clsName)
                    matcher {
                        addUsingString("high_performance_channel_id")
                        addUsingString("ACTION_HIGH_PERFORMANCE")
                        addUsingNumber(5)
                    }
                }.apply {
                    checkDataList("HookBatteryNotify highPerformance")
                    val member = first()
                    member.className.toClass().apply {
                        method { name = member.methodName;emptyParam() }.hook {
                            intercept()
                        }
                    }
                }

                if (highBatteryConsumption) clsName.toClass().apply {
                    method {
                        param(StringClass, BooleanType)
                        paramCount = 2
                    }.hookAll { intercept() }
                }
            }
        }
    }
}
