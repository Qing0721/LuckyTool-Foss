package com.fosstool.app.hook.scope.safecenter

import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.android.ApplicationInfoClass
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.AnyClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.ListClass
import com.highcapable.yukihookapi.hook.type.java.MapClass
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.highcapable.yukihookapi.hook.type.java.UnitType

object UnlockStartupLimitOld : YukiBaseHooker() {
    private const val TAG = "UnlockStartupLimitOld"

    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findClass {
                matcher {
                    fields {
                        addForType(IntType.name)
                        addForType(AnyClass.name)
                        addForType(MapClass.name)
                        addForType(BooleanType.name)
                        addForType(ContextClass.name)
                    }
                    methods {
                        add { paramTypes(ListClass.name) }
                        add { paramTypes(StringClass.name) }
                        add { returnType(UnitType.name) }
                        add { returnType(ListClass.name) }
                        add { returnType(BooleanType.name) }
                        add { returnType(ApplicationInfoClass.name) }
                    }
                    usingStrings("StartupManager")
                }
            }.apply {
                checkDataList(TAG)
                val clazz = firstOrNullSafe()?.name?.toClassOrNull(appClassLoader) ?: return@apply
                clazz.method {
                    param(ContextClass)
                    returnType = UnitType
                }.ignored().hookAll {
                    after {
                        clazz.field { type = IntType }.ignored().get().set(999)
                    }
                }
            }
        }
    }
}
