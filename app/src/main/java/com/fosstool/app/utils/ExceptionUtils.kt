@file:Suppress("unused", "UnusedReceiverParameter")

package com.fosstool.app.utils

import com.highcapable.yukihookapi.hook.log.YLog

inline fun <T> safeOfNull(result: () -> T): T? = safeOf(default = null, result)

inline fun safeOfFalse(result: () -> Boolean) = safeOf(default = false, result)

inline fun safeOfTrue(result: () -> Boolean) = safeOf(default = true, result)

inline fun safeOfNothing(result: () -> String) = safeOf(default = "", result)

inline fun safeOfNan(result: () -> Int) = safeOf(default = 0, result)

inline fun <T> safeOf(default: T, result: () -> T) = try {
    result()
} catch (_: Throwable) {
    default
}

inline fun <T> T.runInSafe(msg: String = "", block: () -> Unit) {
    runCatching(block).onFailure { if (msg.isNotBlank()) YLog.error(msg = msg, e = it) }
}
