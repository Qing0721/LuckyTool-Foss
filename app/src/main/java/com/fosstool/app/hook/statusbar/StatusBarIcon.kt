package com.fosstool.app.hook.statusbar

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.systemui.BluetoothIconRelated
import com.fosstool.app.hook.scope.systemui.FluidCloudIconBackgroundTransparency
import com.fosstool.app.hook.scope.systemui.MobileDataIconRelated
import com.fosstool.app.hook.scope.systemui.RemoveGreenCapsulePrompt
import com.fosstool.app.hook.scope.systemui.RemoveGreenDotPrivacyPrompt
import com.fosstool.app.hook.scope.systemui.RemoveHighPerformanceModeIcon
import com.fosstool.app.hook.scope.systemui.RemoveStatusBarSecurePayment
import com.fosstool.app.hook.scope.systemui.RemoveWiFiDataInout
import com.fosstool.app.hook.scope.systemui.StatusBarIconVerticalCenter
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK

object StatusBarIcon : YukiBaseHooker() {
    override fun onHook() {
        if (prefs(ModulePrefs).getBoolean("remove_statusbar_securepayment_icon", false)) {
            loadHooker(RemoveStatusBarSecurePayment)
        }
        if (prefs(ModulePrefs).getBoolean("remove_wifi_data_inout", false)) {
            loadHooker(RemoveWiFiDataInout)
        }
        loadHooker(MobileDataIconRelated)
        loadHooker(BluetoothIconRelated)
        if (prefs(ModulePrefs).getBoolean("remove_high_performance_mode_icon", false)) {
            loadHooker(RemoveHighPerformanceModeIcon)
        }
        if (prefs(ModulePrefs).getBoolean("remove_green_dot_privacy_prompt", false)) {
            loadHooker(RemoveGreenDotPrivacyPrompt)
        }
        if (prefs(ModulePrefs).getBoolean("remove_green_capsule_prompt", false)) {
            loadHooker(RemoveGreenCapsulePrompt)
        }
        if (prefs(ModulePrefs).getBoolean("status_bar_icon_vertical_center", false)) {
            if (SDK <= A13) loadHooker(StatusBarIconVerticalCenter)
        }
        if (prefs(ModulePrefs).getInt("custom_fluid_cloud_icon_background_transparency", -1) >= 0) {
            loadHooker(FluidCloudIconBackgroundTransparency)
        }

    }
}
