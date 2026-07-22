package com.fosstool.app.hook.scope.oplusgames

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.ListClass
import com.highcapable.yukihookapi.hook.type.java.UnitType

object RemoveWelfarePage : YukiBaseHooker() {
    override fun onHook() {
        "business.mainpanel.MainPanelView".toClass().apply {
            method {
                param { it[0] == ListClass && it[1] == BooleanType }
                paramCount(2..3)
                returnType = UnitType
            }.hook {
                before {
                    val list = args().first().list<Any>().toMutableList()
                    list.removeLastOrNull() ?: return@before
                    if (list.size >= 2) {
                        YLog.debug("${this@RemoveWelfarePage.packageName}\nError -> RemoveWelfarePage")
                        return@before
                    }
                    args().first().set(java.util.ArrayList(list))
                }
            }
        }
    }
}
