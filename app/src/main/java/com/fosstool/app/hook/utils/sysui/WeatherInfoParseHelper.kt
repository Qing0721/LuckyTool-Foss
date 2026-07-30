package com.fosstool.app.hook.utils.sysui

import android.content.Context
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClass
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.StringClass

@Suppress("unused", "MemberVisibilityCanBePrivate")
class WeatherInfoParseHelper(val classLoader: ClassLoader?) {

    val clazz: Class<*> = VariousClass(
        "com.oplusos.systemui.keyguard.clock.WeatherInfoParseHelper",
        "com.oplus.systemui.keyguard.clock.WeatherInfoParseHelper",

        "com.oplus.systemui.shared.clocks.WeatherInfoParseHelper",
    ).get(classLoader)

    val holderClazz: Class<*>? = "${clazz.name}\$HolderInnerClass".toClassOrNull(classLoader)

    val weatherInfoClazz: Class<*> = "${clazz.name}\$WeatherInfo".toClass(classLoader)

    val timeInfoClazz: Class<*>? = "${clazz.name}\$TimeInfo".toClassOrNull(classLoader)

    fun getInstance(): Any? {
        val helperClazz = clazz
        holderClazz?.let { holder ->
            runCatching { holder.field { type = helperClazz }.get().any() }
                .getOrNull()?.let { return it }
        }
        return runCatching {
            clazz.method {
                name = "getInstance"
                emptyParam()
            }.get().call()
        }.getOrNull()
    }

    fun getLocalTimeInfo(context: Context): Any? {
        return clazz.method {
            name = "getLocalTimeInfo"
            param(ContextClass)
        }.get(getInstance()).call(context)
    }

    fun getResidentTimeInfo(context: Context, residentTimeZone: String): Any? {
        return clazz.method {
            name = "getResidentTimeInfo"
            param(ContextClass, StringClass)
        }.get(getInstance()).call(context, residentTimeZone)
    }
}
