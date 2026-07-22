package com.fosstool.app.hook.scope.android

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.fosstool.app.utils.ModulePrefs

object EnableKeepNotificationWhenAppStop : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("enable_keep_notification_when_app_stop", false)) return
        runCatching {
            "com.android.server.notification.OplusNotificationManagerServiceExtImpl".toClass().apply {
                method {
                    name = "shouldKeepNotifcationWhenForceStop"
                    returnType = BooleanType
                }.hook {
                    before {
                        val imp = args().last().int()
                        if (imp == 10020 || imp == 10021) resultTrue()
                    }
                }
            }
        }
    }
}
