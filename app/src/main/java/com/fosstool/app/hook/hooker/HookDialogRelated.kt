package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.securepay.RemoveSecurePayFoundVirusDialog
import com.fosstool.app.hook.scope.systemui.DisableDuplicateFloatingWindow
import com.fosstool.app.hook.scope.systemui.DisableHeadphoneHighVolumeWarning
import com.fosstool.app.hook.scope.systemui.ForceShowToastIcon
import com.fosstool.app.hook.scope.systemui.RemoveLowBatteryDialogWarning
import com.fosstool.app.hook.scope.systemui.RemoveStartRecordingOrCastingDialog
import com.fosstool.app.hook.scope.systemui.RemoveUSBConnectDialog
import com.fosstool.app.hook.scope.systemui.RunFloatingWindowTasksInForeground
import com.fosstool.app.hook.scope.systemui.VolumeDialogWhiteBackground
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getOSVersionCode

class HookDialogRelated : YukiBaseHooker() {
    override fun onHook() {
        if (packageName == "com.android.systemui") {
            if (prefs(ModulePrefs).getBoolean("disable_duplicate_floating_window", false)) {
                loadHooker(DisableDuplicateFloatingWindow)
            }
            if (prefs(ModulePrefs).getBoolean("disable_headphone_high_volume_warning", false)) {
                loadHooker(DisableHeadphoneHighVolumeWarning)
            }
            if (prefs(ModulePrefs).getBoolean("remove_low_battery_dialog_warning", false)) {
                loadHooker(RemoveLowBatteryDialogWarning)
            }
            if (prefs(ModulePrefs).getBoolean("remove_usb_connect_dialog", false)) {
                loadHooker(RemoveUSBConnectDialog)
            }
            loadHooker(VolumeDialogWhiteBackground)
            if (prefs(ModulePrefs).getBoolean("remove_start_recording_or_casting_dialog", false)) {
                loadHooker(RemoveStartRecordingOrCastingDialog)
            }
            if (prefs(ModulePrefs).getBoolean("run_floating_window_tasks_in_foreground", false) &&
                getOSVersionCode in 26..33
            ) {
                loadHooker(RunFloatingWindowTasksInForeground)
            }
            if (prefs(ModulePrefs).getBoolean("force_show_toast_icon", false)) {
                loadHooker(ForceShowToastIcon)
            }
        }

        if (packageName == "com.coloros.securepay") {
            if (prefs(ModulePrefs).getBoolean("remove_secure_pay_found_virus_dialog", false) ||
                prefs(ModulePrefs).getBoolean("remove_payment_protection_virus_dialog", false)
            ) {
                loadHooker(RemoveSecurePayFoundVirusDialog)
            }
        }

        if (packageName == "com.oplus.exsystemservice") {
            if (prefs(ModulePrefs).getBoolean("remove_low_battery_dialog_warning", false)) {
                loadHooker(RemoveLowBatteryDialogWarning)
            }
            if (prefs(ModulePrefs).getBoolean("disable_headphone_high_volume_warning", false)) {
                loadHooker(DisableHeadphoneHighVolumeWarning)
            }
        }
    }
}
