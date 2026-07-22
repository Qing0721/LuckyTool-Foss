package com.fosstool.app.hook.utils.sysui

import android.content.Context
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.hook.scope.android.HookSystemProperties.toClass

@Suppress("unused", "MemberVisibilityCanBePrivate")
class WeatherInfoParseHelper(val classLoader: ClassLoader?) {

    val clazz = "com.oplusos.systemui.keyguard.clock.WeatherInfoParseHelper".toClass(classLoader)
    val weatherInfoClazz =
        "com.oplusos.systemui.keyguard.clock.WeatherInfoParseHelper\$WeatherInfo".toClass(
            classLoader
        )

    fun getInstance(): Any? {
        return clazz.method {
            name = "getInstance"
            emptyParam()
        }.get().call()
    }

    fun getLocalTimeInfo(context: Context): Any? {
        return clazz.method {
            name = "getLocalTimeInfo"
            paramCount = 1
        }.get(getInstance()).call(context)
    }

    fun getResidentTimeInfo(context: Context, residentTimeZone: String): Any? {
        return clazz.method {
            name = "getResidentTimeInfo"
            paramCount = 1
        }.get(getInstance()).call(context, residentTimeZone)
    }
}
