package com.fosstool.app.hook.scope.weather

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.constructor
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.type.java.BooleanType

object Enable15DayWeatherExpandList : YukiBaseHooker() {

    private const val TAG = "Enable15DayWeatherExpandList"
    private const val LOG_TAG = "LuckyTool"

    override fun onHook() {
        val itemClass = "com.oplus.weather.main.view.itemview.FutureDayWeatherItem"
            .toClassOrNull(appClassLoader)
        if (itemClass == null) {
            YLog.error("$TAG -> FutureDayWeatherItem not found", tag = LOG_TAG)
        } else {
            val expandField = itemClass.field {
                name = "isAllow15DayExpand"
                type = BooleanType
                superClass()
            }.ignored().give()
            if (expandField == null) {
                YLog.error("$TAG -> isAllow15DayExpand field not found", tag = LOG_TAG)
            } else {
                itemClass.constructor {
                    param { types -> types.any { it == BooleanType } }
                }.ignored().hookAll {
                    before {
                        val index = args.indexOfFirst { it is Boolean }
                        if (index >= 0) args(index).set(true)
                    }
                }
            }
        }

        val uiConfigManager = "com.oplus.weather.uiconfig.UIConfigManager"
            .toClassOrNull(appClassLoader)
        if (uiConfigManager == null) {
            YLog.error("$TAG -> UIConfigManager not found", tag = LOG_TAG)
            return
        }
        uiConfigManager.method {
            name { it.startsWith("get") && it.contains("Day15ExpandConfig") }
            superClass()
        }.ignored().hookAll { replaceToTrue() }
    }
}
