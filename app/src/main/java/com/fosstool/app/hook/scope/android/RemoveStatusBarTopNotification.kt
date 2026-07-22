package com.fosstool.app.hook.scope.android

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.ModulePrefs

object RemoveStatusBarTopNotification : YukiBaseHooker() {
    override fun onHook() {
        val isEnable = prefs(ModulePrefs).getBoolean("remove_statusbar_top_notification", false)

        "com.android.server.wm.AlertWindowNotification".toClass().apply {
            method { name = "onPostNotification" }.hook {
                if (isEnable) intercept()
            }
        }
    }
}
