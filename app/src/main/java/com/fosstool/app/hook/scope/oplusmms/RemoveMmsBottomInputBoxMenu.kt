package com.fosstool.app.hook.scope.oplusmms

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.ListClass

object RemoveMmsBottomInputBoxMenu : YukiBaseHooker() {
    override fun onHook() {
        runCatching {
            "com.opos.smart.mms.interfaces.netmsg.menu.MenuInfoBaseBean".toClass().apply {
                method {
                    name = "getMenus"
                    returnType = ListClass
                }.hook { replaceTo(java.util.ArrayList<Any>()) }
            }
        }
    }
}
