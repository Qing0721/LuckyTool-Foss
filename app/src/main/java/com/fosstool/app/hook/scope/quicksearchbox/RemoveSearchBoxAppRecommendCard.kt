package com.fosstool.app.hook.scope.quicksearchbox

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.ListClass

object RemoveSearchBoxAppRecommendCard : YukiBaseHooker() {
    override fun onHook() {

        VariousClass(
            "com.heytap.quicksearchbox.ui.widget.AliveAppRecommendView",
            "com.heytap.quicksearchbox.ui.widget.advicesub.AliveAppRecommendView",
        ).toClassOrNull(appClassLoader)?.apply {
            method {
                param { it[0] == ListClass && it[1] == BooleanType }
                paramCount(2..4)
            }.hook {
                before {

                    (args.getOrNull(0) as? MutableList<*>)?.clear()
                }
            }
        }
    }
}
