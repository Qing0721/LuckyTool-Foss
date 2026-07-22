package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.replaceSpace

object RemoveSmallWindowReplyWhitelist : YukiBaseHooker() {
    override fun onHook() {
        var list = prefs(ModulePrefs).getString("set_small_window_reply_blacklist", "")
            .ifBlank {
                prefs(ModulePrefs).getString("set_small_window_reply_blacklist_list", "None")
            }
        dataChannel.wait<String>("set_small_window_reply_blacklist") { list = it }
        dataChannel.wait<String>("set_small_window_reply_blacklist_list") { list = it }

        VariousClass(
            "com.oplusos.systemui.notification.base.BaseNotificationContentInflater",
            "com.oplus.systemui.statusbar.NotificationListenerExtImpl"
        ).toClass().apply {
            method { name = "showSmallWindowReply" }.hook {
                after {
                    val packName = args().first().string()
                    if (list.isBlank() || list == "None") resultTrue()
                    else {
                        val listString = list.replaceSpace
                        val blacklist = if (list.contains("\n")) {
                            listString.split("\n").toMutableList().apply {
                                removeIf { it.isBlank() }
                            }
                        } else arrayListOf(listString)
                        if (blacklist.contains(packName)) resultFalse()
                    }
                }
            }
        }
    }
}
