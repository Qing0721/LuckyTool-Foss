package com.fosstool.app.hook.scope.oplusmms

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.ListClass
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object RemoveMmsBottomInputBoxMenu : YukiBaseHooker() {
    override fun onHook() {
        runCatching {
            "com.opos.smart.mms.interfaces.netmsg.menu.MenuInfoBaseBean".toClassOrNull(appClassLoader)?.apply {
                method {
                    name = "getMenus"
                    returnType = ListClass
                }.hook { replaceTo(java.util.ArrayList<Any>()) }
            }
        }
    }
}
