package com.fosstool.app.hook.utils

import android.content.ContentResolver
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClass

@Suppress("unused", "PropertyName")
class SettingsUtils(val classLoader: ClassLoader?) {

    val clazz = "android.provider.Settings".toClass(classLoader)

    val Global = "android.provider.Settings\$Global".toClass(classLoader)
    val Secure = "android.provider.Settings\$Secure".toClass(classLoader)
    val System = "android.provider.Settings\$System".toClass(classLoader)

    fun Class<*>.getIntForUser(cr: ContentResolver, key: String, userHandle: Int): Int? {
        return this.method {
            name = "getIntForUser"
            paramCount = 3
        }.get().invoke<Int>(cr, key, userHandle)
    }

    fun Class<*>.getIntForUser(
        cr: ContentResolver, key: String, def: Int, userHandle: Int
    ): Int? {
        return this.method {
            name = "getIntForUser"
            paramCount = 4
        }.get().invoke<Int>(cr, key, def, userHandle)
    }

    fun Class<*>.getLongForUser(cr: ContentResolver, key: String, userHandle: Int): Long? {
        return this.method {
            name = "getLongForUser"
            paramCount = 3
        }.get().invoke<Long>(cr, key, userHandle)
    }

    fun Class<*>.getLongForUser(
        cr: ContentResolver, key: String, def: Long, userHandle: Int
    ): Long? {
        return this.method {
            name = "getLongForUser"
            paramCount = 4
        }.get().invoke<Long>(cr, key, def, userHandle)
    }

    fun Class<*>.getFloatForUser(cr: ContentResolver, key: String, userHandle: Int): Float? {
        return this.method {
            name = "getFloatForUser"
            paramCount = 3
        }.get().invoke<Float>(cr, key, userHandle)
    }

    fun Class<*>.getFloatForUser(
        cr: ContentResolver, key: String, def: Float, userHandle: Int
    ): Float? {
        return this.method {
            name = "getFloatForUser"
            paramCount = 4
        }.get().invoke<Float>(cr, key, def, userHandle)
    }

    fun Class<*>.getStringForUser(cr: ContentResolver, key: String, userHandle: Int): String? {
        return this.method {
            name = "getStringForUser"
            paramCount = 3
        }.get().invoke<String>(cr, key, userHandle)
    }

    fun Class<*>.putIntForUser(
        cr: ContentResolver, key: String, value: Int, userHandle: Int
    ): Boolean? {
        return this.method {
            name = "putIntForUser"
            paramCount = 4
        }.get().invoke<Boolean>(cr, key, value, userHandle)
    }

    fun Class<*>.putLongForUser(
        cr: ContentResolver, key: String, value: Long, userHandle: Int
    ): Boolean? {
        return this.method {
            name = "putLongForUser"
            paramCount = 4
        }.get().invoke<Boolean>(cr, key, value, userHandle)
    }

    fun Class<*>.putFloatForUser(
        cr: ContentResolver, key: String, value: Float, userHandle: Int
    ): Boolean? {
        return this.method {
            name = "putFloatForUser"
            paramCount = 4
        }.get().invoke<Boolean>(cr, key, value, userHandle)
    }

    fun Class<*>.putString(
        cr: ContentResolver, key: String, value: String, overrideableByRestore: Boolean
    ): Boolean? {
        return this.method {
            name = "putString"
            paramCount = 4
        }.get().invoke<Boolean>(cr, key, value, overrideableByRestore)
    }

    fun Class<*>.putStringForUser(
        cr: ContentResolver, key: String, value: String, userHandle: Int
    ): Boolean? {
        return this.method {
            name = "putStringForUser"
            paramCount = 4
        }.get().invoke<Boolean>(cr, key, value, userHandle)
    }

    fun Class<*>.putStringForUser(
        cr: ContentResolver, key: String, value: String, userHandle: Int,
        overrideableByRestore: Boolean
    ): Boolean? {
        return this.method {
            name = "putStringForUser"
            paramCount = 5
        }.get().invoke<Boolean>(cr, key, value, userHandle, overrideableByRestore)
    }

    fun Class<*>.putStringForUser(
        cr: ContentResolver, key: String, value: String, tag: String,
        makeDefault: Boolean, userHandle: Int, overrideableByRestore: Boolean
    ): Boolean? {
        return this.method {
            name = "putStringForUser"
            paramCount = 7
        }.get().invoke<Boolean>(cr, key, value, tag, makeDefault, userHandle, overrideableByRestore)
    }
}
