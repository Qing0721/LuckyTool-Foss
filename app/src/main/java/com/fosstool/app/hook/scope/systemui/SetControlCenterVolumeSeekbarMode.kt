package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.ModulePrefs

object SetControlCenterVolumeSeekbarMode : YukiBaseHooker() {
    override fun onHook() {
        val mode = prefs(ModulePrefs).getString("set_control_center_volume_seekbar_mode", "0")
        if (mode == "0") return

        "com.oplusos.systemui.common.feature.QSFeatureOption".toClass().apply {
            method { name = "isSupportVolumeSeekBar" }.hook {
                before {
                    if (mode == "1") resultTrue() else if (mode == "2") resultFalse()
                }
            }
        }
    }
}
