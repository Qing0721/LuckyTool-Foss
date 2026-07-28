package com.fosstool.app.hook.statusbar

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.systemui.ControlCenterClockStyle
import com.fosstool.app.hook.scope.systemui.ControlCenterDateStyle
import com.fosstool.app.hook.scope.systemui.ControlCenterProgressPercent
import com.fosstool.app.hook.scope.systemui.ControlCenterSliderTransparency
import com.fosstool.app.hook.scope.systemui.ControlCenterWhiteBackground
import com.fosstool.app.hook.scope.systemui.EnableNotificationAlignBothSides
import com.fosstool.app.hook.scope.systemui.NfcDelayShutdown
import com.fosstool.app.hook.scope.systemui.RemoveControlCenterEditMoreButton
import com.fosstool.app.hook.scope.systemui.RemoveControlCenterUserSwitcher
import com.fosstool.app.hook.scope.systemui.RemoveStatusBarBottomNetworkWarn
import com.fosstool.app.hook.scope.systemui.SpecialTileGaps
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getOSVersionCode

object StatusBarControlCenter : YukiBaseHooker() {
    override fun onHook() {
        loadHooker(ControlCenterClockStyle)

        loadHooker(ControlCenterDateStyle)

        if (prefs(ModulePrefs).getBoolean("enable_notification_align_both_sides", false)) {
            loadHooker(EnableNotificationAlignBothSides)
        }

        if (prefs(ModulePrefs).getBoolean("remove_control_center_user_switcher", false)) {
            if (getOSVersionCode < 26) loadHooker(RemoveControlCenterUserSwitcher)
        }
        loadHooker(RemoveStatusBarBottomNetworkWarn)

        loadHooker(ControlCenterWhiteBackground)

        loadHooker(ControlCenterProgressPercent)
        loadHooker(NfcDelayShutdown)
        loadHooker(SpecialTileGaps)
        loadHooker(ControlCenterSliderTransparency)
        loadHooker(RemoveControlCenterEditMoreButton)

    }
}
