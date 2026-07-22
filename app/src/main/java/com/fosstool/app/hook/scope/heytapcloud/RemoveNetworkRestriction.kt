package com.fosstool.app.hook.scope.heytapcloud

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList

object RemoveNetworkRestriction : YukiBaseHooker() {
    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findClass {
                matcher {
                    methods {
                        add { paramCount(0);returnType(IntType.name) }
                        add { paramCount(1..2);returnType(BooleanType.name) }
                        add { paramTypes(IntType.name);returnType(IntType.name) }
                        add { paramTypes(IntType.name);returnType(BooleanType.name) }
                        add { paramTypes(ContextClass.name);returnType(BooleanType.name) }
                        add { paramTypes(ContextClass.name);returnType(StringClass.name) }
                    }
                    usingStrings(
                        "NetworkUtil",
                        "connectivity",
                        "getNetworkTypeString",
                        "isMobileDataNetwork",
                        "isNetworkConnected"
                    )
                }
            }.apply {
                checkDataList("RemoveNetworkRestriction")
                first().name.toClass().apply {
                    method { emptyParam();returnType = IntType }.giveAll().forEach {
                        it.hook {
                            after { if (result<Int>() == 1) result = 2 }
                        }
                    }
                }
            }
        }
    }
}
