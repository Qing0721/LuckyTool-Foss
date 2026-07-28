package com.fosstool.app.hook.utils

import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.utils.SDK
import com.fosstool.app.utils.getOSVersionCode

@Suppress("unused", "MemberVisibilityCanBePrivate", "PrivatePropertyName")
class OplusBuildUtlils(val classLoader: ClassLoader? = null) {

    val clazz: Class<*> = "com.oplus.os.OplusBuild".toClassOrNull(classLoader)
        ?: runCatching { Class.forName("com.oplus.os.OplusBuild") }.getOrNull()
        ?: Any::class.java

    private val VERSIONS = arrayOf(
        "V1.0", "V1.2", "V1.4", "V2.0", "V2.1", "V3.0", "V3.1", "V3.2", "V5.0", "V5.1",
        "V5.2", "V6.0", "V6.1", "V6.2", "V6.7", "V7", "V7.1", "V7.2", "V11", "V11.1",
        "V11.2", "V11.3", "V12", "V12.1", "V12.2", "V13", "V13.1", "V13.1.1", "V13.2", "V14.0",
        "V14.0.1", "V14.1", "V15.0", "V15.1", "V15.2", "V16.0.0", "V16.0.1", "V16.1", "V16.2", "V16.3",
        "V17.0"
    )

    private val getOSVersions get() = try {
        clazz.field { name = "VERSIONS" }.get().cast<Array<String>>()
    } catch (_: Throwable) { null }

    val getOSVersionCode get() = try {
        clazz.method { name = "getOplusOSVERSION" }.get().invoke<Int>()
    } catch (_: Throwable) { null }

    val getOSVersionName: String? get() {
        val code = getOSVersionCode ?: return null
        val systemVersions = getOSVersions
        if (systemVersions != null && code - 1 < systemVersions.size) {
            return systemVersions[code - 1]
        }
        return if (code - 1 < VERSIONS.size) VERSIONS[code - 1] else null
    }
}

fun requireOsV(range: IntRange): Boolean = getOSVersionCode in range

fun requireSdk(range: IntRange): Boolean = SDK in range
