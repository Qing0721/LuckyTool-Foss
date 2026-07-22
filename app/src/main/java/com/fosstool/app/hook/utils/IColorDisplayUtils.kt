package com.fosstool.app.hook.utils

import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType

@Suppress("unused", "MemberVisibilityCanBePrivate")
class IColorDisplayUtils(val classLoader: ClassLoader?) {

    val serviceName = "color_display"
    val clazz = "android.hardware.display.ColorDisplayManager".toClass(classLoader)
    val internal = "android.hardware.display.ColorDisplayManager\$ColorDisplayManagerInternal"
        .toClass(classLoader)

    fun getInstance(): Any? {
        return internal.method {
            name = "getInstance"
            emptyParam()
        }.get().call()
    }

    fun isReduceBrightColorsActivated(instance: Any?): Boolean? {
        return internal.method {
            name = "isReduceBrightColorsActivated"
            emptyParam()
        }.get(instance).invoke<Boolean>()
    }

    fun setReduceBrightColorsActivated(instance: Any?, activated: Boolean): Boolean? {
        return internal.method {
            name = "setReduceBrightColorsActivated"
            param(BooleanType)
        }.get(instance).invoke<Boolean>(activated)
    }
}
