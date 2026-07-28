package com.fosstool.app.hook.scope.systemui

import android.view.View
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.safeOfNull

object ControlCenterWhiteBackground : YukiBaseHooker() {
    override fun onHook() {
        var customAlpha =
            prefs(ModulePrefs).getInt("custom_control_center_background_transparency", -1)
        dataChannel.wait<Int>("custom_control_center_background_transparency") {
            customAlpha = it
        }

        "com.android.systemui.statusbar.phone.ScrimController"
            .toClassOrNull(appClassLoader)
            ?.method { name = "updateScrimColor" }?.ignored()?.hook {
                before {
                    if (customAlpha < 0) return@before
                    val value = customAlpha / 10.0F
                    val view = args.getOrNull(0) as? View ?: return@before
                    val alpha = args.getOrNull(1) as? Float ?: return@before
                    val name = safeOfNull { view.resources.getResourceEntryName(view.id) }
                        ?: return@before
                    when (name) {
                        "scrim_in_front" -> {}
                        "scrim_behind" -> if (alpha > value) args[1] = value
                        "scrim_notifications" -> if (alpha > value) args[1] = value
                    }
                }
            }
    }
}
