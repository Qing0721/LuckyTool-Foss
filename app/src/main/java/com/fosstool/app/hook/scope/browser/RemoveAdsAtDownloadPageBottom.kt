package com.fosstool.app.hook.scope.browser

import android.view.View
import androidx.core.view.isVisible
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList

object RemoveAdsAtDownloadPageBottom : YukiBaseHooker() {
    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findMethod {
                matcher {
                    paramCount(0)
                    returnType(UnitType.name)
                    usingNumbers(0, 8, 500L)
                    addUsingField {
                        addWriteMethod {
                            paramTypes("com.heytap.browser.downloads.entity.RecommendConfig")
                            returnType(UnitType.name)
                        }
                        type("com.heytap.browser.downloads.entity.RecommendConfig")
                    }
                    addUsingField {
                        addWriteMethod {
                            paramCount(0)
                            returnType(UnitType.name)
                        }
                        type("android.widget.LinearLayout")
                    }
                    addUsingField {
                        addWriteMethod {
                            paramCount(0)
                            returnType(UnitType.name)
                        }
                        type("com.coui.appcompat.tablayout.COUITabLayout")
                    }
                }
            }.apply {
                checkDataList("RemoveAdsAtDownloadPageBottom")
                val member = first()
                member.className.toClass().apply {
                    method {
                        name = member.methodName
                        emptyParam()
                        returnType(UnitType)
                    }.hook {
                        replaceUnit {
                            field { type("android.widget.LinearLayout") }.get(instance)
                                .cast<View>()?.isVisible = false
                        }
                    }
                }
            }
        }
    }
}
