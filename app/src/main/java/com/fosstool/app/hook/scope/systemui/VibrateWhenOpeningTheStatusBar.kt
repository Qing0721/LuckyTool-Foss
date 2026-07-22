package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.constructor
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.hasField
import com.highcapable.yukihookapi.hook.factory.method

object VibrateWhenOpeningTheStatusBar : YukiBaseHooker() {
    override fun onHook() {
        VariousClass(
            "com.android.systemui.statusbar.phone.PanelViewController",
            "com.android.systemui.shade.NotificationPanelViewController"
        ).toClass().apply {
            constructor().hook {
                after { field { name = "mVibrateOnOpening" }.get(instance).setTrue() }
            }
        }

        VariousClass(
            "com.android.systemui.statusbar.phone.StatusBarCommandQueueCallbacks",
            "com.android.systemui.statusbar.phone.CentralSurfacesCommandQueueCallbacks"
        ).toClass().apply {
            if (hasField { name = "mVibrateOnOpening" }.not()) return@apply
            constructor().hook {
                after { field { name = "mVibrateOnOpening" }.get(instance).setTrue() }
            }
        }

        "com.android.systemui.statusbar.phone.StatusBar".toClassOrNull()?.apply {
            if (hasField { name = "mVibrateOnOpening" }.not()) return@apply
            method { name = "start" }.hook {
                after { field { name = "mVibrateOnOpening" }.get(instance).setTrue() }
            }
        }
    }
}
