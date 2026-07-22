package com.fosstool.app.hook.statusbar

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.systemui.CustomNotificationBackgroundTransparency
import com.fosstool.app.hook.scope.systemui.DisableHighVolumeWarningNotification
import com.fosstool.app.hook.scope.systemui.EnableGlobalNotificationSimpleBannerMode
import com.fosstool.app.hook.scope.systemui.RemoveChargingCompleted
import com.fosstool.app.hook.scope.systemui.RemoveDanmakuNotificationWhitelist
import com.fosstool.app.hook.scope.systemui.RemoveDoNotDisturbModeNotification
import com.fosstool.app.hook.scope.systemui.RemoveFlashlightOpenNotification
import com.fosstool.app.hook.scope.systemui.RemoveGTModeNotification
import com.fosstool.app.hook.scope.systemui.RemoveNotificationCleanupButton
import com.fosstool.app.hook.scope.systemui.RemoveNotificationForMuteNotifications
import com.fosstool.app.hook.scope.systemui.RemoveSmallWindowReplyWhitelist
import com.fosstool.app.hook.scope.systemui.RemoveStatusBarDevMode
import com.fosstool.app.utils.ModulePrefs

object StatusBarNotify : YukiBaseHooker() {
    override fun onHook() {
        if (prefs(ModulePrefs).getBoolean("remove_charging_completed", false)) {
            loadHooker(RemoveChargingCompleted)
        }
        if (prefs(ModulePrefs).getBoolean("remove_statusbar_devmode", false)) {
            loadHooker(RemoveStatusBarDevMode)
        }
        if (prefs(ModulePrefs).getBoolean("remove_flashlight_open_notification", false)) {
            loadHooker(RemoveFlashlightOpenNotification)
        }
        if (prefs(ModulePrefs).getBoolean("remove_do_not_disturb_mode_notification", false)) {
            loadHooker(RemoveDoNotDisturbModeNotification)
        }
        if (prefs(ModulePrefs).getBoolean("remove_notifications_for_mute_notifications", false)) {
            loadHooker(RemoveNotificationForMuteNotifications)
        }
        if (prefs(ModulePrefs).getBoolean("remove_gt_mode_notification", false)) {
            loadHooker(RemoveGTModeNotification)
        }
        if (prefs(ModulePrefs).getBoolean("remove_small_window_reply_whitelist", false)) {
            loadHooker(RemoveSmallWindowReplyWhitelist)
        }
        if (prefs(ModulePrefs).getBoolean("remove_danmaku_notification_whitelist", false)) {
            loadHooker(RemoveDanmakuNotificationWhitelist)
        }
        if (prefs(ModulePrefs).getInt("custom_notification_background_transparency", -1) >= 0) {
            loadHooker(CustomNotificationBackgroundTransparency)
        }
        loadHooker(DisableHighVolumeWarningNotification)
        loadHooker(EnableGlobalNotificationSimpleBannerMode)
        loadHooker(RemoveNotificationCleanupButton)
    }
}
