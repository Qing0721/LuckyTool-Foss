package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.systemui.AutoWakeUpFaceUnlockNotification
import com.fosstool.app.hook.scope.systemui.ForceDisplayClockStyleOptions
import com.fosstool.app.hook.scope.systemui.ForceEnableScreenOffMusicSupport
import com.fosstool.app.hook.scope.systemui.HideLockScreenStatusBarDisplay
import com.fosstool.app.hook.scope.systemui.LockScreenBottomButton
import com.fosstool.app.hook.scope.systemui.LockScreenCarriers
import com.fosstool.app.hook.scope.systemui.LockScreenChargingComponent
import com.fosstool.app.hook.scope.systemui.LockScreenClock
import com.fosstool.app.hook.scope.systemui.LockScreenComponent
import com.fosstool.app.hook.scope.systemui.LockScreenCustomClockComponentStyle
import com.fosstool.app.hook.scope.systemui.LockScreenShowRealChargingTechnology
import com.fosstool.app.hook.scope.systemui.RemoveAodMusicWhitelist
import com.fosstool.app.hook.scope.systemui.RemoveLockScreenBottomSOSButton
import com.fosstool.app.hook.scope.systemui.RemoveLockScreenClockComponent
import com.fosstool.app.hook.scope.systemui.RemoveLockScreenCloseNotificationButton
import com.fosstool.app.hook.scope.systemui.RemoveTopLockScreenIcon
import com.fosstool.app.hook.scope.systemui.ReplaceChargingTechnologyDrawingStyle
import com.fosstool.app.hook.scope.systemui.StatusbarCustomCarrierDisplayText
import com.fosstool.app.hook.utils.OplusBuildUtlils
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK

object HookLockScreen : YukiBaseHooker() {
    override fun onHook() {

        loadHooker(LockScreenClock())

        loadHooker(LockScreenComponent)

        loadHooker(LockScreenChargingComponent)

        loadHooker(LockScreenBottomButton)

        loadHooker(LockScreenCarriers)

        if (prefs(ModulePrefs).getBoolean("remove_lock_screen_bottom_sos_button", false)) {
            if (SDK >= A13) loadHooker(RemoveLockScreenBottomSOSButton)
        }

        if (prefs(ModulePrefs).getBoolean("remove_top_lock_screen_icon", false)) {
            loadHooker(RemoveTopLockScreenIcon)
        }

        if (prefs(ModulePrefs).getBoolean("remove_lock_screen_close_notification_button", false)) {
            if ((OplusBuildUtlils().getOSVersionCode ?: 0) < 33) loadHooker(RemoveLockScreenCloseNotificationButton)
        }

        if (prefs(ModulePrefs).getBoolean("remove_aod_music_whitelist", false)) {
            if (SDK >= A13) loadHooker(RemoveAodMusicWhitelist)
        }
        if (prefs(ModulePrefs).getBoolean("force_enable_screen_off_music_support", false) &&
            (OplusBuildUtlils().getOSVersionCode ?: 0) >= 26 &&
            (OplusBuildUtlils().getOSVersionCode ?: 0) < 34
        ) {
            loadHooker(ForceEnableScreenOffMusicSupport)
        }

        if (prefs(ModulePrefs).getBoolean("hide_lock_screen_status_bar_display", false)) {
            loadHooker(HideLockScreenStatusBarDisplay)
        }
        if (prefs(ModulePrefs).getBoolean("auto_wake_up_face_unlock_notification", false)) {
            loadHooker(AutoWakeUpFaceUnlockNotification)
        }

        if (prefs(ModulePrefs).getBoolean("force_display_clock_style_options", false) && SDK == A13) {
            loadHooker(ForceDisplayClockStyleOptions)
        }
        if (prefs(ModulePrefs).getBoolean("lock_screen_show_real_charging_technology", false)) {
            loadHooker(LockScreenShowRealChargingTechnology)
        }
        if (prefs(ModulePrefs).getBoolean("remove_lock_screen_clock_component", false)) {
            loadHooker(RemoveLockScreenClockComponent)
        }
        loadHooker(LockScreenCustomClockComponentStyle)
        loadHooker(ReplaceChargingTechnologyDrawingStyle)
        loadHooker(StatusbarCustomCarrierDisplayText)
    }
}
