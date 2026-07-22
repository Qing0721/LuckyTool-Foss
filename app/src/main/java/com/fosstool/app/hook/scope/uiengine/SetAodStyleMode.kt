package com.fosstool.app.hook.scope.uiengine

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.A14
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK

object SetAodStyleMode : YukiBaseHooker() {

    override fun onHook() {
        val fossMode = prefs(ModulePrefs).getString("set_aod_style_mode", "0") ?: "0"
        val mode = if (fossMode != "0") fossMode
            else prefs(ModulePrefs).getString("set_aod_notification_icon_style", "0") ?: "0"

        "com.oplus.egview.util.ProductFlavorOption".toClass().apply {
            val methodName = if (SDK >= A14) "isFlavorTwoDeviceExp" else "isFlavorTwoDevice"
            method { name = methodName }.hook {
                when (mode) {
                    "1" -> replaceToTrue()
                    "2" -> replaceToFalse()
                }
            }
        }
    }
}
