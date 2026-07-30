package com.fosstool.app.hook.scope.browser

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object RemoveAdsFromDownloadDialog : YukiBaseHooker() {
    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->

            dexKitBridge.findMethod {
                matcher {
                    declaredClass {
                        addFieldForType(ContextClass.name)
                        addFieldForType(StringClass.name)
                        addMethod {
                            paramTypes(ContextClass.name, IntType.name)
                            returnType(UnitType.name)
                        }
                        usingStrings("DownloadCardAdProvider")
                    }
                    usingStrings("DownloadCardAdProvider", "createAdRequest", "appName", "posIds")
                }
            }.apply {
                checkDataList("RemoveAdsFromDownloadDialog")
                firstOrNullSafe()?.apply {
                    className.toClassOrNull(appClassLoader)
                        ?.method { name = methodName }
                        ?.ignored()
                        ?.hook { intercept() }
                }
            }
        }
    }
}
