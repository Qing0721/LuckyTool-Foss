package com.fosstool.app.hook.scope.browser

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList

object RemoveBrowserWindowLimitNumber : YukiBaseHooker() {
    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findMethod {
                matcher {
                    paramCount = 0
                    returnType = IntType.name
                    usingStrings("TabManager", "multiWindowPerf")
                }
            }.apply {
                checkDataList("RemoveBrowserWindowLimitNumber")
                first().apply {
                    className.toClass().method {
                        name = methodName
                        returnType = IntType
                    }.hook { replaceTo(999) }
                }
            }
        }
    }
}
