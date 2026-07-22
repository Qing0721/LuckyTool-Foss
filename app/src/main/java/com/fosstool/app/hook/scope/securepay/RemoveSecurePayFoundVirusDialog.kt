package com.fosstool.app.hook.scope.securepay

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.CheckBoxClass
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.android.DialogInterfaceClass
import com.highcapable.yukihookapi.hook.type.android.ViewClass
import com.highcapable.yukihookapi.hook.type.defined.VagueType
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList

object RemoveSecurePayFoundVirusDialog : YukiBaseHooker() {
    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findClass {
                searchPackages("com.coloros.securepay")
                matcher {
                    fields {
                        addForType(BooleanType.name)
                        addForType(CheckBoxClass.name)
                    }
                    methods {
                        add { paramCount(0);returnType(UnitType.name) }
                        add { paramCount(4..8);returnType(UnitType.name) }
                        add { paramCount(0);returnType(BooleanType.name) }
                        add { paramTypes(ViewClass.name);returnType(UnitType.name) }
                        add {
                            paramTypes(
                                ContextClass.name,
                                StringClass.name,
                                IntType.name,
                                DialogInterfaceClass.name,
                                IntType.name
                            )
                            returnType(UnitType.name)
                        }
                    }
                }
            }.apply {
                checkDataList("RemoveSecurePayFoundVirusDialog")
                first().name.toClass().apply {
                    method {
                        param(VagueType, StringClass)
                        returnType = UnitType
                    }.hook { intercept() }
                    method {
                        emptyParam()
                        returnType = UnitType
                    }.hookAll {
                        intercept()
                    }
                }
            }
        }
    }
}
