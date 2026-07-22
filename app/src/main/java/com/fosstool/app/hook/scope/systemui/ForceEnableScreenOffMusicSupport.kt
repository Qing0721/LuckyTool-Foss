package com.fosstool.app.hook.scope.systemui

import android.content.Context
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.hook.utils.SettingsUtils
import com.fosstool.app.utils.A14
import com.fosstool.app.utils.SDK

object ForceEnableScreenOffMusicSupport : YukiBaseHooker() {
    override fun onHook() {
        VariousClass(
            "com.oplus.systemui.keyguard.OplusBlackScreenGestureControllExImpl",
            "com.oplus.systemui.keyguard.gesture.OplusBlackScreenGestureControllExImpl"
        ).toClass().apply {
            method { name = "resetAodMediaSupportConfig" }.hook {
                after {
                    val context = field { name = "mContext" }.get(instance).cast<Context>()
                        ?: return@after
                    SettingsUtils(appClassLoader).Secure.method {
                        name = "putIntForUser";paramCount = 4
                    }.get().call(context.contentResolver, "aod_media_support", 1, 0)
                    val utilCls =
                        if (SDK >= A14) "com.oplus.systemui.aod.mediapanel.util.AodMediaStatisticUtil"
                        else "com.oplusos.systemui.notification.util.NotificationStatisticUtil"
                    utilCls.toClass().method { name = "setAodMediaSupport";paramCount = 1 }.get()
                        .call(true)
                }
            }
        }

    }
}
