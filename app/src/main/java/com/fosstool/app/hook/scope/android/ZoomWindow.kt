package com.fosstool.app.hook.scope.android

import android.util.ArraySet
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.BundleClass
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.fosstool.app.utils.ModulePrefs

object ZoomWindow : YukiBaseHooker() {
    override fun onHook() {
        var isEnable = prefs(ModulePrefs).getBoolean("enable_zoom_window", false)
        dataChannel.wait<Boolean>("enable_zoom_window") { isEnable = it }
        var supportList = prefs(ModulePrefs).getStringSet("zoom_window_support_list", ArraySet())
        dataChannel.wait<Set<String>>("zoom_window_support_list") { supportList = it }

        "com.android.server.wm.OplusZoomWindowConfig".toClass().apply {
            method {
                name = "isSupportZoomMode"
                param(StringClass, IntType, StringClass, BundleClass)
            }.hook {
                before {
                    if (!isEnable) return@before
                    val target = args(0).string()
                    val packName = if (target.contains("/")) {
                        target.split("/").takeIf { e -> e.isNotEmpty() }?.get(0)
                            ?: return@before
                    } else target
                    if (supportList.contains(packName)) resultTrue()
                }
            }
        }
    }
}
