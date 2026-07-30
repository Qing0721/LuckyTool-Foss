package com.fosstool.app.hook.scope.browser

import android.view.View
import androidx.core.view.isVisible
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object RemoveAdsAtDownloadPageBottom : YukiBaseHooker() {
    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->

            val recommendConfig = listOf(
                "com.heytap.browser.downloads.entity.RecommendConfig",
                "com.heytap.browser.download.ui.downloadlist.model.RecommendConfig",
            ).firstNotNullOfOrNull { it.toClassOrNull(appClassLoader) } ?: return@create
            dexKitBridge.findMethod {
                matcher {
                    paramCount(0)
                    returnType(UnitType.name)
                    usingNumbers(0, 8, 500L)
                    addUsingField {
                        addWriteMethod {
                            paramTypes(recommendConfig.name)
                            returnType(UnitType.name)
                        }
                        type(recommendConfig.name)
                    }
                    addUsingField {
                        addWriteMethod {
                            paramCount(0)
                            returnType(UnitType.name)
                        }
                        type("android.widget.LinearLayout")
                    }
                }
            }.apply {
                checkDataList("RemoveAdsAtDownloadPageBottom")
                val member = firstOrNullSafe() ?: return@apply
                member.className.toClassOrNull(appClassLoader)?.apply {
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
