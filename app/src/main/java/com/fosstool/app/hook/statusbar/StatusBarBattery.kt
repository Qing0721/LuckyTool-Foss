package com.fosstool.app.hook.statusbar

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.systemui.StatusBarBatteryInfoNotify
import com.fosstool.app.hook.scope.systemui.StatusBarPower
import com.fosstool.app.utils.A12
import com.fosstool.app.utils.SDK

object StatusBarBattery : YukiBaseHooker() {
    override fun onHook() {
        loadHooker(StatusBarPower)

        if (SDK >= A12) loadHooker(StatusBarBatteryInfoNotify)
    }
}
