package com.fosstool.app.hook.scope.settings

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.IntType

object EnableSwipeUpNavigationGesture : YukiBaseHooker() {
    override fun onHook() {
        "com.oplus.settings.feature.navbar.NavBarSettingsValueUtil".toClass().apply {
            method {
                name = "getGestureUpModeAvailable"
                param(ContextClass)
                returnType = IntType
            }.hook {
                replaceTo(0)
            }
        }
    }
}
