package com.fosstool.app.hook.scope.launcher

import android.util.ArraySet
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.BundleClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getOSVersionCode

object CustomAppFloatingWindowDisplayMode : YukiBaseHooker() {
    override fun onHook() {
        var displayMode = prefs(ModulePrefs).getString("custom_app_floating_window_display_mode", "0") ?: "0"
        dataChannel.wait<String>("custom_app_floating_window_display_mode") { displayMode = it }
        var supportList = prefs(ModulePrefs).getStringSet("zoom_window_support_list", ArraySet())
        dataChannel.wait<Set<String>>("zoom_window_support_list") { supportList = it }

        runCatching {
            "com.android.server.wm.OplusZoomWindowConfig".toClass().apply {
                method {
                    name = "isSupportZoomMode"
                    param(StringClass, IntType, StringClass, BundleClass)
                    returnType = BooleanType
                }.hook {
                    before {
                        when (displayMode) {
                            "1" -> resultFalse()
                            "2" -> resultTrue()
                            "3" -> {
                                val target = args(0).string()
                                val packName = if (target.contains("/")) {
                                    target.split("/").takeIf { it.isNotEmpty() }?.get(0) ?: return@before
                                } else target
                                if (supportList.contains(packName)) resultTrue()
                            }
                        }
                    }
                }
            }
        }

        if (getOSVersionCode >= 33) {
            runCatching {
                "com.android.server.wm.FlexibleWindowUtils".toClass().apply {
                    method {
                        name = "isSupportFlexibleWindow"
                        param(StringClass, StringClass)
                        returnType = BooleanType
                    }.hook {
                        before {
                            when (displayMode) {
                                "1" -> resultFalse()
                                "2" -> resultTrue()
                                "3" -> {
                                    val target = args(0).string()
                                    val packName = if (target.contains("/")) {
                                        target.split("/").takeIf { it.isNotEmpty() }?.get(0) ?: return@before
                                    } else target
                                    if (supportList.contains(packName)) resultTrue()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
