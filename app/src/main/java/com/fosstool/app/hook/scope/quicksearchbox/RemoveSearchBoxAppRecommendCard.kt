package com.fosstool.app.hook.scope.quicksearchbox

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.ListClass

object RemoveSearchBoxAppRecommendCard : YukiBaseHooker() {
    override fun onHook() {
        "com.heytap.quicksearchbox.ui.widget.AliveAppRecommendView".toClass().apply {
            method {
                param { it[0] == ListClass && it[1] == BooleanType && it[2] == BooleanType }
                paramCount(3..4)
            }.hook {
                before {
                    args().first().list<Any>().toMutableList().apply {
                        clear()
                        args().first().set(ArrayList(this))
                    }
                }
            }
        }
    }
}
