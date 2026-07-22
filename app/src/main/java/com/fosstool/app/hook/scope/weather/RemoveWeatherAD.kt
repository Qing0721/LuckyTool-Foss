package com.fosstool.app.hook.scope.weather

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.fosstool.app.utils.ModulePrefs

object RemoveWeatherAD : YukiBaseHooker() {
    override fun onHook() {
        val disableJump = prefs(ModulePrefs).getBoolean("disable_weather_jump_browser", false)

        "com.oplus.weather.utils.LocalUtils".toClass().apply {
            method { name = "startBrowserForUrl" }.hook {
                before {
                    args(0).set(0)
                    args(2).set("${args[2]}&infoEnable=false")
                    args().last().set(true)
                }
            }
            method { name = "jumpToBrowser" }.hook() {
                before {
                    args(2).set("${args[2]}&infoEnable=false")
                }
            }
            if (disableJump) {
                method {
                    name = "isBrowserSupportJump"
                    returnType = BooleanType
                }.hook { replaceToFalse() }
            }
        }
    }
}
