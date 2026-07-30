package com.fosstool.app.hook.scope.battery

import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.UnitType

object UnlockStartupLimit : YukiBaseHooker() {
    private const val TAG = "UnlockStartupLimit"

    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findMethod {
                matcher {
                    declaredClass {
                        fields {
                            addForType(ContextClass.name)
                            add { type = "com.oplus.startupapp.data.database.RecordDatabase" }
                        }
                        usingStrings("StartupManager")
                        methods {
                            add {
                                paramCount(0)
                                returnType(IntType.name)
                            }
                            add {
                                paramTypes("android.content.Intent")
                                returnType(UnitType.name)
                            }
                            add {
                                paramTypes("android.os.Bundle")
                                returnType(UnitType.name)
                            }
                        }
                    }
                    paramCount(0)
                    returnType(IntType.name)
                    usingNumbers(5, 20)
                }
            }.apply {
                checkDataList(TAG)
                firstOrNullSafe()?.let { member ->
                    member.className.toClassOrNull(appClassLoader)
                        ?.method {
                            name = member.methodName
                            emptyParam()
                            returnType = IntType
                        }
                        ?.ignored()
                        ?.hook { replaceTo(999) }
                }
            }
        }
    }
}
