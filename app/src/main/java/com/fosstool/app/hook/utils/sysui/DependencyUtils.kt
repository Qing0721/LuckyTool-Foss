package com.fosstool.app.hook.utils.sysui

import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

@Suppress("unused", "MemberVisibilityCanBePrivate")
class DependencyUtils(val classLoader: ClassLoader?, val useEx: Boolean = false) {

    val clazz: Class<*>? =
        (if (useEx) "com.android.systemui.DependencyEx" else "com.android.systemui.Dependency")
            .toClassOrNull(classLoader)

    val instance: Any?
        get() {
            val cls = clazz ?: return null
            return runCatching { cls.field { type = cls }.get().any() }.getOrNull()
        }

    fun get(cls: Class<*>): Any? {
        val dependency = clazz ?: return null
        val ins = instance
        if (ins != null) {
            val result = runCatching {
                dependency.method {
                    name = "getDependency"
                    param(Class::class.java)
                }.get(ins).invoke<Any>(cls)
            }.getOrNull()
            if (result != null) return result
        }

        return runCatching {
            dependency.method {
                name = "get"
                param(Class::class.java)
            }.get().invoke<Any>(cls)
        }.getOrNull()
    }
}
