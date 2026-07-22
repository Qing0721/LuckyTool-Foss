package com.fosstool.app.hook.scope.systemui

import android.view.View
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.safeOfNull

object ControlCenterWhiteBackground : YukiBaseHooker() {
    override fun onHook() {
        var customAlpha =
            prefs(ModulePrefs).getInt("custom_control_center_background_transparency", -1)
        dataChannel.wait<Int>("custom_control_center_background_transparency") {
            customAlpha = it
        }

        "com.android.systemui.statusbar.phone.ScrimController".toClass().apply {
            method { name = "updateScrimColor" }.hook {
                before {
                    if (customAlpha < 0) return@before
                    val value = customAlpha / 10.0F
                    val view = args().first().cast<View>() ?: return@before
                    val alpha = args(1).cast<Float>() ?: return@before
                    val name = safeOfNull { view.resources.getResourceEntryName(view.id) }
                        ?: return@before
                    when (name) {
                        "scrim_in_front" -> {}
                        "scrim_behind" -> if (alpha > value) args(1).set(value)
                        "scrim_notifications" -> if (alpha > value) args(1).set(value)
                    }
                }
            }
        }
    }
}
