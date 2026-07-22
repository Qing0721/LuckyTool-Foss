package com.fosstool.app.hook.scope.android

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK

object RemoveAccessDeviceLogDialog : YukiBaseHooker() {

    override fun onHook() {
        val isEnable = prefs(ModulePrefs).getBoolean("remove_access_device_log_dialog", false)
        if (SDK < A13) return

        "com.android.server.logcat.LogcatManagerService".toClass().apply {
            method { name = "processNewLogAccessRequest" }.hook {
                before {
                    if (!isEnable) return@before
                    val client = args().first().any() ?: return@before
                    instance.current().method { name = "onAccessApprovedForClient" }.call(client)
                    resultNull()
                }
            }
        }
    }
}
