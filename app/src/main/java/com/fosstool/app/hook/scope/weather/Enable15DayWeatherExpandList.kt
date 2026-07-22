package com.fosstool.app.hook.scope.weather

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.BooleanType

object Enable15DayWeatherExpandList : YukiBaseHooker() {
    override fun onHook() {
        runCatching {
            "com.oplus.weather.main.view.itemview.FutureDayWeatherItem".toClass().apply {
                method {
                    name = "isAllow15DayExpand"
                    returnType = BooleanType
                }.hook { replaceToTrue() }
            }
        }
    }
}
