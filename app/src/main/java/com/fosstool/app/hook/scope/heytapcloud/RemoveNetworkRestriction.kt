package com.fosstool.app.hook.scope.heytapcloud

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object RemoveNetworkRestriction : YukiBaseHooker() {
    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findClass {
                matcher {
                    methods {
                        add {
                            paramCount(0)
                            returnType(IntType.name)
                            usingStrings("connectivity")
                            usingNumbers(0, 1, 2)
                        }
                        add {
                            paramTypes(IntType.name)
                            returnType(BooleanType.name)
                        }
                        add {
                            paramTypes(ContextClass.name)
                            returnType(BooleanType.name)
                            usingStrings("NetworkUtil", "connectivity", "isMobileDataNetwork")
                        }
                        add {
                            paramTypes(ContextClass.name)
                            returnType(BooleanType.name)
                            usingStrings("NetworkUtil", "connectivity", "isNetworkConnected")
                        }
                    }
                }
            }.apply {
                checkDataList("RemoveNetworkRestriction")
                (firstOrNullSafe()?.name ?: return@apply).toClassOrNull(appClassLoader)?.apply {
                    method { emptyParam(); returnType = IntType }.giveAll().forEach {
                        it.hook {
                            after { if (result<Int>() == 1) result = 2 }
                        }
                    }
                }
            }
        }
    }
}
