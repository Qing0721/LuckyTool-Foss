package com.fosstool.app.hook.utils

import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.LongType
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.fosstool.app.hook.scope.systemui.StatusBarBatteryInfoNotify.toClass

@Suppress("unused", "MemberVisibilityCanBePrivate")
class SystemPropertiesUtils(classLoader: ClassLoader?) {

    val clazz = "android.os.SystemProperties".toClass(classLoader)

    fun get(key: String): String? {
        return clazz.method {
            name = "get"
            param(StringClass)
        }.get().invoke<String>(key)
    }

    fun get(key: String, defValue: String?): String? {
        return clazz.method {
            name = "get"
            param(StringClass, StringClass)
        }.get().invoke<String>(key, defValue)
    }

    fun getBoolean(key: String, defValue: Boolean?): Boolean? {
        return clazz.method {
            name = "getBoolean"
            param(StringClass, BooleanType)
        }.get().invoke<Boolean>(key, defValue)
    }

    fun getInt(key: String, defValue: Int?): Int? {
        return clazz.method {
            name = "getInt"
            param(StringClass, IntType)
        }.get().invoke<Int>(key, defValue)
    }

    fun getLong(key: String, defValue: Long?): Long? {
        return clazz.method {
            name = "getLong"
            param(StringClass, LongType)
        }.get().invoke<Long>(key, defValue)
    }

    fun set(key: String, value: String?) {
        clazz.method {
            name = "set"
            param(StringClass, StringClass)
        }.get().call(key, value)
    }
}
