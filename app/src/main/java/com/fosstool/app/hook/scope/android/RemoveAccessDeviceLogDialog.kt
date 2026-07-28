package com.fosstool.app.hook.scope.android

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.utils.ModulePrefs
import de.robv.android.xposed.XposedHelpers

object RemoveAccessDeviceLogDialog : YukiBaseHooker() {

    override fun onHook() {

        val isEnable = prefs(ModulePrefs).getBoolean("remove_access_device_log_dialog", false)
        if (!isEnable) return

        val cls = "com.android.server.logcat.LogcatManagerService".toClassOrNull(appClassLoader) ?: return
        cls.method { name = "processNewLogAccessRequest" }.ignored().hook {
            before {
                val client = args(0).any() ?: return@before
                runCatching {
                    XposedHelpers.callMethod(instance, "onAccessApprovedForClient", client)
                }
                resultNull()
            }
        }
    }
}
