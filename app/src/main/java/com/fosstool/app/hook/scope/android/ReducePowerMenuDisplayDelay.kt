package com.fosstool.app.hook.scope.android

import android.view.KeyEvent
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.ModulePrefs

object ReducePowerMenuDisplayDelay : YukiBaseHooker() {
    override fun onHook() {
        var isEnable = prefs(ModulePrefs).getBoolean("reduce_power_menu_display_delay", false)
        dataChannel.wait<Boolean>("reduce_power_menu_display_delay") { isEnable = it }

        "com.android.server.policy.SingleKeyGestureDetectorExtImpl".toClass().apply {
            method { name = "modifyPressTimeout" }.hook {
                after {
                    if (!isEnable) return@after
                    val pressType = args().first().cast<Int>() ?: return@after
                    val event = args().last().cast<KeyEvent>() ?: return@after
                    if (pressType == 1 && event.keyCode == 26) result = 800L
                }
            }
        }
    }
}
