package com.fosstool.app.hook.scope.android

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.constructor
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getOSVersionCode

object ScreenColorTemperatureRGBPalette : YukiBaseHooker() {
    override fun onHook() {
        val isEnable =
            prefs(ModulePrefs).getBoolean("enable_screen_color_temperature_rgb_palette", false)
        if (getOSVersionCode < 27 || !isEnable) return

        "com.android.server.display.oplus.eyeprotect.manager.OplusRgbBallManager".toClass().apply {
            constructor { emptyParam() }.hook {
                after {
                    field { name = "mIsSupportColorModeRGB" }.get(instance).setTrue()
                    method { name = "initRGBValueAnimator" }.get(instance).call()
                }
            }
        }
    }
}
