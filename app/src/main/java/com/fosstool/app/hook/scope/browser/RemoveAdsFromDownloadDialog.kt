package com.fosstool.app.hook.scope.browser

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList

object RemoveAdsFromDownloadDialog : YukiBaseHooker() {
    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findClass {
                matcher {
                    fields {
                        addForType(ContextClass.name)
                        addForType(StringClass.name)
                        addForType("com.opos.feed.api.FeedAdNative")
                        addForType("com.opos.feed.api.RecyclerAdHelper")
                        addForType("com.opos.feed.api.params.AdInteractionListener")
                    }
                    methods {
                        add {
                            paramTypes(ContextClass.name, IntType.name)
                            returnType(UnitType.name)
                        }
                        add { returnType("com.opos.feed.api.params.AdRequest") }
                        add { returnType("com.opos.feed.api.RecyclerAdHelper") }
                    }
                    usingStrings("DownloadCardAdProvider")
                }
            }.apply {
                checkDataList("RemoveAdsFromDownloadDialog")
                first().name.toClass().apply {
                    method {
                        paramCount(1)
                        returnType("com.opos.feed.api.params.AdRequest")
                    }.hook { replaceTo(null) }
                }
            }
        }
    }
}
