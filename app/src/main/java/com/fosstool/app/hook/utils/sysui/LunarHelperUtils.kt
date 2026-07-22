package com.fosstool.app.hook.utils.sysui

import android.content.Context
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.factory.buildOf
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.LongType
import com.fosstool.app.hook.statusbar.StatusBarClock.toClass

class LunarHelperUtils(val classLoader: ClassLoader?) {

    val clazz = VariousClass(
        "com.oplusos.systemui.keyguard.clock.LunarHelper",
        "com.oplus.systemui.keyguard.clock.LunarHelper"
    ).toClass(classLoader)

    fun buildInstance(context: Context): Any? {
        return clazz.buildOf(context) {
            param(ContextClass)
        }
    }

    fun getDateToString(instance: Any?, time: Long): String? {
        return clazz.method {
            name = "getDateToString"
            param(LongType)
        }.get(instance).invoke<String>(time)
    }
}
