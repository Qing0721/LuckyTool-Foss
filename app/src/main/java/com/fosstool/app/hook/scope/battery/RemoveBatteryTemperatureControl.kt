package com.fosstool.app.hook.scope.battery

import android.os.Looper
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.android.ContentResolverClass
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.android.HandlerClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.UnitType

object RemoveBatteryTemperatureControl : YukiBaseHooker() {
    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            runCatching {
                dexKitBridge.findClass {
                    matcher {
                        fields { addForType(ContextClass.name) }
                        methods { add { name = "handleMessage" } }
                        usingStrings("ThermalControl", "Handler")
                    }
                }.apply {
                    checkDataList("RemoveBatteryTemperatureControl find ThermalControlHandler")
                    firstOrNullSafe()?.name?.toClassOrNull(appClassLoader)
                        ?.method { name = "handleMessage" }
                        ?.ignored()
                        ?.hook { intercept() }
                }
            }

            runCatching {
                dexKitBridge.findClass {
                    matcher { usingStrings("ThermalControllerCenter") }
                }.apply {
                    checkDataList("RemoveBatteryTemperatureControl find ThermalControllerCenter")

                    firstOrNullSafe()?.name?.toClassOrNull(appClassLoader)
                        ?.method { param(Looper::class.java) }
                        ?.ignored()
                        ?.hookAll { intercept() }
                }
            }

            runCatching {
                val monitor = dexKitBridge.findClass {
                    matcher { usingStrings("ThermalControlMonitor") }
                }.checkDataList("RemoveBatteryTemperatureControl find ThermalControlMonitor")
                if (monitor.isNotEmpty()) {

                    dexKitBridge.findMethod {
                        searchInClass(monitor)
                        matcher {
                            paramCount(0)
                            returnType(UnitType.name)
                            usingFields {
                                add { type = BooleanType.name }
                                add { type = HandlerClass.name }
                                add { type = ContentResolverClass.name }
                                add { type = "android.database.ContentObserver" }
                            }
                            addInvoke {
                                paramCount(0)
                                returnType(UnitType.name)
                            }
                        }
                    }.apply {
                        checkDataList("RemoveBatteryTemperatureControl find startMonitor")
                        firstOrNullSafe()?.let { member ->
                            member.className.toClassOrNull(appClassLoader)
                                ?.method { name = member.methodName; emptyParam() }
                                ?.ignored()
                                ?.hook { intercept() }
                        }
                    }
                }
            }

            runCatching {
                "com.oplus.thermalcontrol.ThermalControlUtils".toClassOrNull(appClassLoader)
                    ?.method { param(Looper::class.java) }
                    ?.ignored()
                    ?.hook { intercept() }
            }
        }
    }
}
