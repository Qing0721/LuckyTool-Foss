package com.fosstool.app.hook.scope.android

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.ModulePrefs

object RemoveSystemPromptIcon : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("remove_statusbar_top_notification", false)) return
        runCatching {
            "com.android.server.wm.AlertWindowNotification".toClass().apply {
                method { name = "onPostNotification" }.hook { before { resultNull() } }
            }
        }
    }
}
