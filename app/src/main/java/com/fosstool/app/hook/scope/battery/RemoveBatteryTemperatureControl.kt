package com.fosstool.app.hook.scope.battery

import android.os.Looper
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.constructor
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList

object RemoveBatteryTemperatureControl : YukiBaseHooker() {
    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            runCatching {
                dexKitBridge.findClass {
                    matcher {
                        usingStrings("ThermalControl", "Handler")
                    }
                }.apply {
                    checkDataList("RemoveBatteryTemperatureControl ThermalControlHandler")
                    if (isNotEmpty()) {
                        runCatching {
                            first().name.toClass().method {
                                name = "handleMessage"
                                paramCount = 1
                            }.hookAll {
                                before { intercept() }
                            }
                        }
                    }
                }
            }

            runCatching {
                dexKitBridge.findClass {
                    matcher {
                        usingStrings("ThermalControllerCenter")
                    }
                }.apply {
                    checkDataList("RemoveBatteryTemperatureControl ThermalControllerCenter")
                    if (isNotEmpty()) {
                        runCatching {
                            first().name.toClass().constructor {
                                param(Looper::class.java)
                            }.hook {
                                before { intercept() }
                            }
                        }
                    }
                }
            }

            runCatching {
                dexKitBridge.findClass {
                    matcher {
                        usingStrings("ThermalControlMonitor")
                    }
                }.apply {
                    checkDataList("RemoveBatteryTemperatureControl ThermalControlMonitor")
                    if (isNotEmpty()) {
                        runCatching {
                            first().name.toClass().method {
                                name = "startMonitor"
                                paramCount = 4
                            }.hookAll {
                                before { intercept() }
                            }
                        }
                    }
                }
            }

            runCatching {
                dexKitBridge.findMethod {
                    searchPackages("com.oplus.thermalcontrol")
                    matcher {
                        paramTypes(Looper::class.java.name)
                        paramCount(1)
                    }
                }.apply {
                    checkDataList("RemoveBatteryTemperatureControl ThermalControlUtils(Looper)", false)
                    forEach { member ->
                        if (member.className != "com.oplus.thermalcontrol.ThermalControlUtils") return@forEach
                        runCatching {
                            member.className.toClass().method {
                                name = member.methodName
                                paramCount = 1
                            }.hookAll {
                                before { intercept() }
                            }
                        }
                    }
                }
            }
        }
    }
}
