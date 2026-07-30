package com.fosstool.app.hook.scope.battery

import android.app.NotificationManager
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.fosstool.app.utils.ModulePrefs
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.android.HandlerClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.highcapable.yukihookapi.hook.type.java.UnitType
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge

object HookBatteryNotify : YukiBaseHooker() {
    override fun onHook() {
        val highPerformance =
            prefs(ModulePrefs).getBoolean("remove_high_performance_mode_notifications", false)
        val highBatteryConsumption =
            prefs(ModulePrefs).getBoolean("remove_app_high_battery_consumption_warning", false)

        if (!highPerformance && !highBatteryConsumption) return

        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            val classes = dexKitBridge.findClass {
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
            }.checkDataList("HookBatteryNotify NotifyUtil")

            val clsName = classes.firstOrNullSafe()?.name ?: return@create

            if (highPerformance) dexKitBridge.findMethod {
                searchInClass(classes)
                matcher {
                    paramCount(0)
                    returnType(UnitType.name)
                    addUsingString("high_performance_channel_id")
                    addUsingString("ACTION_HIGH_PERFORMANCE")
                    addUsingNumber(5)
                }
            }.apply {
                checkDataList("HookBatteryNotify highPerformance")
                val member = firstOrNullSafe() ?: return@apply
                member.className.toClassOrNull(appClassLoader)
                    ?.method { name = member.methodName; paramCount = 0 }
                    ?.ignored()
                    ?.hook { intercept() }
            }

            if (highBatteryConsumption) {
                clsName.toClassOrNull(appClassLoader)?.declaredMethods
                    ?.filter {
                        it.parameterCount == 2 &&
                            it.parameterTypes[0] == String::class.java &&
                            (it.parameterTypes[1] == Boolean::class.javaPrimitiveType ||
                                it.parameterTypes[1] == java.lang.Boolean::class.java) &&
                            it.returnType == Void.TYPE
                    }
                    ?.forEach { m ->
                        runCatching {
                            XposedBridge.hookMethod(m, object : XC_MethodHook() {
                                override fun beforeHookedMethod(param: MethodHookParam) {
                                    param.result = null
                                }
                            })
                        }
                    }
            }
        }
    }
}
