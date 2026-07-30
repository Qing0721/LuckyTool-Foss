package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getOSVersionCode
import com.fosstool.app.utils.replaceSpace

object RemoveSmallWindowReplyWhitelist : YukiBaseHooker() {
    override fun onHook() {
        if (getOSVersionCode >= 34) {
            "com.android.systemui.util.HeadsUpToZoomUtils"
                .toClassOrNull(appClassLoader)
                ?.method { name { it.contains("isZoom") } }?.ignored()?.hook { replaceToTrue() }
            return
        }

        var list = prefs(ModulePrefs).getString("set_small_window_reply_blacklist", "")
            .ifBlank {
                prefs(ModulePrefs).getString("set_small_window_reply_blacklist_list", "None")
            }
        dataChannel.wait<String>("set_small_window_reply_blacklist") { list = it }
        dataChannel.wait<String>("set_small_window_reply_blacklist_list") { list = it }

        VariousClass(
            "com.oplusos.systemui.notification.base.BaseNotificationContentInflater",
            "com.oplus.systemui.statusbar.NotificationListenerExtImpl"
        ).toClassOrNull(appClassLoader)
            ?.method { name = "showSmallWindowReply" }?.ignored()?.hook {
                before {
                    val packName = args.getOrNull(0)?.toString() ?: return@before
                    if (list.isBlank() || list == "None") {

                        result = true
                    } else {
                        val listString = list.replaceSpace
                        val blacklist = if (list.contains("\n")) {
                            listString.split("\n").toMutableList().apply {
                                removeIf { it.isBlank() }
                            }
                        } else arrayListOf(listString)

                        result = !blacklist.contains(packName)
                    }
                }
            }
    }
}
