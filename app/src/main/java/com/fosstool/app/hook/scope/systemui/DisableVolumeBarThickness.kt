package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClass
import com.highcapable.yukihookapi.hook.log.YLog
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getOSVersionCode

object DisableVolumeBarThickness : YukiBaseHooker() {
    override fun onHook() {
        if (!(prefs(ModulePrefs).getBoolean("disable_volume_bar_thickness", false) ||
                prefs(ModulePrefs).getBoolean("disable_volume_bar_thickness_effect", false))
        ) return

        if (getOSVersionCode < 30) return

        try {
            "com.oplus.systemui.volume.OplusVolumeDialogImpl".toClass().apply {
                method { name = "startThickAnim" }.hook {
                    intercept()
                }
            }
        } catch (e: Throwable) {
            YLog.error("DisableVolumeBarThickness: OplusVolumeDialogImpl not found", tag = "LuckyTool")
        }
    }
}
