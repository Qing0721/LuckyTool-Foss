package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.ModulePrefs

object DisableHighVolumeWarningNotification : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("disable_high_volume_warning_notifications", false)) return

        VariousClass(
            "com.oplusos.systemui.notification.receiver.VolumeReceiver",
            "com.oplus.systemui.statusbar.receiver.VolumeReceiver",
            "com.oplusos.systemui.notification.power.OplusPowerUI",
            "com.oplus.systemui.statusbar.notification.power.OplusPowerUI"
        ).toClass().apply {
            method { name = "start" }.hook {
                before { resultNull() }
            }
        }
    }
}
