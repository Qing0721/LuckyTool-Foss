package com.fosstool.app.hook.scope.permissioncontroller

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import org.luckypray.dexkit.query.enums.UsingType

object UnlockDefaultDesktopLimit : YukiBaseHooker() {
    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            val forceMethod = dexKitBridge.findMethod {
                matcher {
                    addUsingField {
                        addWriteMethod {
                            paramTypes(ContextClass.name)
                            returnType(UnitType.name)
                            usingStrings(
                                "oplus.software.pms_app_frozen",
                                "oplus.software.defaultapp.remove_force_launcher",
                                "oplus.hardware.type.tablet"
                            )
                        }
                        addReadMethods {
                            paramCount(0)
                            returnType(BooleanType.name)
                        }
                        usingType(UsingType.Read)
                    }
                    paramCount(0)
                    returnType(BooleanType.name)
                    addCaller {
                        declaredClass {
                            usingStrings("DefaultApp")
                        }
                        paramTypes("java.util.List")
                        returnType(UnitType.name)
                        usingStrings("DefaultApp")
                    }
                }
            }.checkDataList("UnlockDefaultDesktopLimit allMethod", false)

            val tableMethod = dexKitBridge.findMethod {
                searchPackages(forceMethod.first().className)
                matcher {
                    addUsingField {
                        addWriteMethod {
                            paramTypes(ContextClass.name)
                            returnType(UnitType.name)
                            usingStrings(
                                "oplus.software.pms_app_frozen",
                                "oplus.software.defaultapp.remove_force_launcher",
                                "oplus.hardware.type.tablet"
                            )
                        }
                        addReadMethods {
                            paramCount(0)
                            returnType(BooleanType.name)
                        }
                        usingType(UsingType.Read)
                    }
                    paramCount(0)
                    returnType(BooleanType.name)
                    addCaller {
                        declaredClass {
                            usingStrings("DefaultApp")
                        }
                        paramTypes("java.util.List")
                        returnType(UnitType.name)
                        usingStrings("DefaultApp")
                    }
                    addCaller {
                        declaredClass {
                            usingStrings("DefaultApp")
                        }
                        paramCount(6)
                        returnType(UnitType.name)
                        usingStrings("DefaultApp")
                    }
                }
            }.checkDataList("UnlockDefaultDesktopLimit tableMethod")

            val tableMethodData = tableMethod.first()
            val finalMethod = org.luckypray.dexkit.result.MethodDataList().apply {
                addAll(forceMethod.filterNot { it == tableMethodData })
            }
            finalMethod.checkDataList("UnlockDefaultDesktopLimit finalMethod")
            val member = finalMethod.first()
            member.className.toClass().apply {
                method { name = member.methodName }.hook {
                    replaceToTrue()
                }
            }
        }
    }
}
