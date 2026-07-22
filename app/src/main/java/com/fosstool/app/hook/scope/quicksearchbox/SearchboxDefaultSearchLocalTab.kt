package com.fosstool.app.hook.scope.quicksearchbox

import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClass
import com.highcapable.yukihookapi.hook.type.java.StringClass

object SearchboxDefaultSearchLocalTab : YukiBaseHooker() {
    override fun onHook() {
        val hooked = runCatching {
            "com.heytap.quicksearchbox.ui.fragment.SearchResultFragment".toClass().method {
                name = "getDefaultTabId"
                emptyParam()
                returnType = StringClass
            }.hook { replaceTo("local") }
            true
        }.getOrDefault(false)
        if (hooked) return

        DexkitUtils.create(appInfo.sourceDir) { bridge ->
            bridge.findMethod {
                searchPackages("com.heytap.quicksearchbox.ui.fragment")
                matcher {
                    paramCount = 0
                    returnType(StringClass.name)
                    addUsingField {
                        type("com.heytap.quicksearchbox.core.localsearch.SearchParams")
                    }
                    addUsingField {
                        type("com.heytap.common.bean.TabItems")
                    }
                }
            }.apply {
                checkDataList("SearchboxDefaultSearchLocalTab", onlyOne = false)
                forEach { data ->
                    data.className.toClass().method {
                        name = data.methodName
                        emptyParam()
                        returnType = StringClass
                    }.hook { replaceTo("local") }
                }
            }
        }
    }
}
