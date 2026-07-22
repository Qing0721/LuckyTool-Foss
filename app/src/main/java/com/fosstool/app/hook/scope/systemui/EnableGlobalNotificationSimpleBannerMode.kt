package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.ModulePrefs

object EnableGlobalNotificationSimpleBannerMode : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("enable_global_notification_simple_banner_mode", false)) return

        VariousClass(
            "com.oplusos.systemui.notification.helper.FullScreenBannerHelper",
            "com.oplus.systemui.statusbar.notification.helper.FullScreenBannerHelper",
            "com.oplus.systemui.notification.interruption.fullscreenbanner.FullScreenBannerHelper"
        ).toClass().apply {
            method { name = "isSimpleBannerEnable" }.hook {
                before { resultTrue() }
            }
        }
    }
}
