package com.fosstool.app.hook.utils.sysui

import android.content.Context
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.hook.scope.android.HookSystemProperties.toClass

@Suppress("unused", "MemberVisibilityCanBePrivate")
class ClockSwitchHelper(val classLoader: ClassLoader?) {

    val clazz = "com.oplusos.systemui.keyguard.clock.ClockSwitchHelper".toClass(classLoader)

    fun getInstance(context: Context): Any? {
        return clazz.method {
            name = "getInstance"
            paramCount = 1
        }.get().call(context)
    }

    fun getLocatedWeatherInfo(instance: Any): Any? {
        return clazz.method {
            name = "getLocatedWeatherInfo"
            emptyParam()
        }.get(instance).call()
    }

    fun getResidentWeatherInfo(instance: Any): Any? {
        return clazz.method {
            name = "getResidentWeatherInfo"
            emptyParam()
        }.get(instance).call()
    }
}
