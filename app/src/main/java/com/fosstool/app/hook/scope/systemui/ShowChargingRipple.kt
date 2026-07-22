package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.constructor
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.A14
import com.fosstool.app.utils.SDK

object ShowChargingRipple : YukiBaseHooker() {
    override fun onHook() {
        VariousClass(
            "com.android.systemui.statusbar.charging.WiredChargingRippleController",
            "com.android.systemui.charging.WiredChargingRippleController"
        ).toClass().apply {
            constructor().hook {
                after {
                    field { name = "rippleEnabled" }.get(instance).setTrue()
                }
            }

        }
        if (SDK >= A14) return
        "com.android.systemui.statusbar.FeatureFlags".toClass().apply {
            method { name = "isChargingRippleEnabled" }.hook {
                replaceToTrue()
            }
        }
    }
}
