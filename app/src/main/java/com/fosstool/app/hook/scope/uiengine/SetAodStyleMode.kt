package com.fosstool.app.hook.scope.uiengine

import com.fosstool.app.utils.A14
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object SetAodStyleMode : YukiBaseHooker() {

    override fun onHook() {
        val fossMode = prefs(ModulePrefs).getString("set_aod_style_mode", "0") ?: "0"
        val mode = if (fossMode != "0") fossMode
            else prefs(ModulePrefs).getString("set_aod_notification_icon_style", "0") ?: "0"

        val methodName = if (SDK >= A14) "isFlavorTwoDeviceExp" else "isFlavorTwoDevice"
        "com.oplus.egview.util.ProductFlavorOption".toClassOrNull(appClassLoader)
            ?.method { name = methodName }
            ?.ignored()
            ?.hook {
                before {
                    when (mode) {
                        "1" -> result = true
                        "2" -> result = false
                    }
                }
            }
    }
}
