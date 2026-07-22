package com.fosstool.app.hook.scope.browser

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.ListClass
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList

object RemoveBrowserSearchBarAppPromotion : YukiBaseHooker() {
    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            val appClass = runCatching {
                dexKitBridge.findClass {
                    matcher { usingStrings("RemoveBrowserSearchBarAppPromotion App") }
                }.first().name
            }.getOrNull()
            val adsClass = runCatching {
                dexKitBridge.findClass {
                    matcher { usingStrings("RemoveBrowserSearchBarAppPromotion Ads") }
                }.first().name
            }.getOrNull()
            val targets = setOf(appClass, adsClass).filterNotNull()
            dexKitBridge.findMethod {
                matcher {
                    paramTypes(ListClass.name)
                    returnType(UnitType.name)
                    usingStrings("linkEdit")
                }
            }.apply {
                checkDataList("RemoveBrowserSearchBarAppPromotion")
                first().apply {
                    className.toClass().method {
                        name = methodName
                        param(ListClass)
                        returnType = UnitType
                    }.hook {
                        before {
                            if (targets.isEmpty()) return@before
                            val list = args().first().list<Any>()
                            if (list.isEmpty()) return@before
                            args().first().set(ArrayList<Any>(list).apply {
                                removeAll { targets.contains(it?.javaClass?.name) }
                            })
                        }
                    }
                }
            }
        }
    }
}
