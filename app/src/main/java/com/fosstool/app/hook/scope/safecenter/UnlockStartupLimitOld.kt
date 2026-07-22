package com.fosstool.app.hook.scope.safecenter

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.ApplicationInfoClass
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.AnyClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.ListClass
import com.highcapable.yukihookapi.hook.type.java.MapClass
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList

object UnlockStartupLimitOld : YukiBaseHooker() {

    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findClass {
                searchPackages(
                    "com.coloros.safecenter.startupapp",
                    "com.oplus.safecenter.startupapp"
                )
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
                checkDataList("UnlockStartupLimitOld")
                first().name.toClass().apply {
                    method {
                        param(ContextClass)
                        returnType = UnitType
                    }.hookAll {
                        after { field { type = IntType }.get().set(10000) }
                    }
                }
            }
        }
    }
}
