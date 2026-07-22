package com.fosstool.app.hook.scope.battery

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList

object UnlockStartupLimit : YukiBaseHooker() {

    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findMethod {
                matcher {
                    usingStrings("StartupManager")
                }
            }.apply {
                checkDataList("UnlockStartupLimit")
                first().className.toClass().apply {
                    method {
                        param(ContextClass)
                        returnType = UnitType
                    }.hookAll {
                        after { field { type = IntType }.get().set(999) }
                    }
                }
            }
        }
    }
}
