package com.fosstool.app.hook.scope.browser

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe

object RemoveBrowserWindowLimitNumber : YukiBaseHooker() {
    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->

            val scope = dexKitBridge.findClass {
                matcher { className("com.android.browser.TabManager") }
            }
            if (scope.isEmpty()) return@create
            dexKitBridge.findMethod {
                searchInClass(scope)
                matcher {
                    paramCount = 0
                    returnType = IntType.name
                    usingStrings("TabManager", "multiWindowPerf")
                }
            }.apply {
                checkDataList("RemoveBrowserWindowLimitNumber")
                firstOrNullSafe()?.apply {
                    className.toClassOrNull(appClassLoader)
                        ?.method {
                            name = methodName
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
