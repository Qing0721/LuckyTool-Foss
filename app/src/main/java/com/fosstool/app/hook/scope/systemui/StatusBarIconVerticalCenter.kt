package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object StatusBarIconVerticalCenter : YukiBaseHooker() {
    override fun onHook() {
        VariousClass(
            "com.oplusos.systemui.ext.BasePhoneStatusBarViewExt",
            "com.oplus.systemui.statusbar.phone.PhoneStatusBarViewExImpl"
        ).toClassOrNull(appClassLoader)?.let { c ->
            c.method { name = "getHoleTop" }.ignored().hook { replaceTo(0) }
            c.method { name = "getHoleBottom" }.ignored().hook { replaceTo(0) }
        }
    }
}
