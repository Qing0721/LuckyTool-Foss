package com.fosstool.app.hook.scope.systemui

import android.view.View
import androidx.core.view.isVisible
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.constructor
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.type.android.ContextClass

object RemoveRotateScreenButton : YukiBaseHooker() {
    override fun onHook() {
        VariousClass(
            "com.android.systemui.statusbar.phone.FloatingRotationButton",
            "com.android.systemui.navigationbar.gestural.FloatingRotationButton",
            "com.android.systemui.shared.rotation.FloatingRotationButton"
        ).toClass().apply {
            constructor { param { it[0] == ContextClass } }.hook {
                after {
                    field { name = "mKeyButtonView" }.get(instance).cast<View>()?.isVisible = false
                }
            }
        }
    }
}
