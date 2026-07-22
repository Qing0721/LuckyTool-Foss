package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method

object HideInActiveSignalLabelsGen2x2 : YukiBaseHooker() {
    override fun onHook() {
        val cls = VariousClass(
            "com.oplus.systemui.statusbar.policy.MobileIconSets",
            "com.oplusos.systemui.statusbar.policy.MobileIconSets"
        ).toClass( initialize = true)

        "${cls.canonicalName}\$Companion".toClass().apply {
            method { name = "getVolteIcon" }.hook {
                before {
                    val position = args().last().cast<Int>() ?: return@before
                    val volteIconEx = cls.field { name = "VOLTE_ICON_EX" }.get()
                        .cast<IntArray>() ?: return@before
                    result = if (position < 0 || position >= volteIconEx.size) 0
                    else volteIconEx[position]
                }
            }
        }
    }
}
