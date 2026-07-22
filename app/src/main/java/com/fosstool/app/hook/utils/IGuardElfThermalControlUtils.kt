package com.fosstool.app.hook.utils

import android.os.IBinder
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClass
import com.highcapable.yukihookapi.hook.type.android.IBinderClass

@Suppress("unused", "MemberVisibilityCanBePrivate")
class IGuardElfThermalControlUtils(val classLoader: ClassLoader?) {

    val clazz = "android.os.IGuardElfThermalControl".toClass(classLoader)
    val stub = "android.os.IGuardElfThermalControl\$Stub".toClass(classLoader)

    fun getInstance(iBinder: IBinder?): Any? {
        return stub.method {
            name = "asInterface"
            param(IBinderClass)
        }.get().call(iBinder)
    }

    fun getUIsohValue(instance: Any?): Int? {
        return clazz.method {
            name = "getUIsohValue"
        }.get(instance).invoke<Int>()
    }
}
