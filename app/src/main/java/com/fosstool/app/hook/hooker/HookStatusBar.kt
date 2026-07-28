package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.systemui.DisableVolumeBarThickness
import com.fosstool.app.hook.scope.systemui.DoubleClickLockScreen
import com.fosstool.app.hook.scope.systemui.HideInActiveSignalLabelsGen2x2
import com.fosstool.app.hook.scope.systemui.MusicFluidCloudControl
import com.fosstool.app.hook.scope.systemui.VibrateWhenOpeningTheStatusBar
import com.fosstool.app.hook.scope.systemui.VolumeBarPercent
import com.fosstool.app.utils.ModulePrefs

object HookStatusBar : YukiBaseHooker() {
    override fun onHook() {
        if (prefs(ModulePrefs).getBoolean("statusbar_double_click_lock_screen", false)) {
            loadHooker(DoubleClickLockScreen)
        }
        if (prefs(ModulePrefs).getBoolean("vibrate_when_opening_the_statusbar", false)) {
            loadHooker(VibrateWhenOpeningTheStatusBar)
        }
        if (prefs(ModulePrefs).getBoolean("hide_inactive_signal_labels_gen2x2", false)) {
            loadHooker(HideInActiveSignalLabelsGen2x2)
        }
        if (prefs(ModulePrefs).getBoolean("enable_volume_bar_percent_display", false)) {
            loadHooker(VolumeBarPercent)
        }
        loadHooker(DisableVolumeBarThickness)
        loadHooker(MusicFluidCloudControl)

    }
}
