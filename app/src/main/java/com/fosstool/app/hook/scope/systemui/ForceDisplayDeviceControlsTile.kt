package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.ModulePrefs

object ForceDisplayDeviceControlsTile : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("force_display_of_device_controls_tiles", false)) return

        "com.oplus.systemui.qs.tiles.OplusDeviceControlsTile".toClass().apply {
            method { name = "isAvailable" }.hook {
                before { resultTrue() }
            }
        }
    }
}
