package com.fosstool.app.utils

import com.highcapable.yukihookapi.hook.log.YLog
import com.fosstool.app.BuildConfig
import org.luckypray.dexkit.DexKitBridge
import org.luckypray.dexkit.result.ClassDataList
import org.luckypray.dexkit.result.MethodDataList

@Suppress("MemberVisibilityCanBePrivate")
object DexkitUtils {
    const val tag = "LuckyTool"
    val debug = BuildConfig.DEBUG

    fun create(appPath: String, result: (DexKitBridge) -> Unit) {
        System.loadLibrary("dexkit")
        DexKitBridge.create(appPath)?.use { result(it) }
    }

    fun ClassDataList.checkDataList(instance: String, onlyOne: Boolean = true): ClassDataList {
        when {
            isEmpty() -> YLog.error("$instance -> findClass isNullOrEmpty", tag = tag)
            size > 1 && onlyOne -> {
                var find = ""
                forEach { find += "[${it.name}]" }
                YLog.error("$instance -> findClass size ($size) -> $find", tag = tag)
            }
            size == 1 -> if (debug) YLog.debug(
                "$instance -> findClass ${first().name}", tag = tag
            )
        }
        return this
    }

    fun MethodDataList.checkDataList(instance: String, onlyOne: Boolean = true): MethodDataList {
        when {
            isEmpty() -> YLog.error("$instance -> findMethod isNullOrEmpty", tag = tag)
            size > 1 && onlyOne -> {
                var find = ""
                forEach { find += "[${it.className}|${it.methodName}]" }
                YLog.error("$instance -> findMethod size ($size) -> $find", tag = tag)
            }
            size == 1 -> if (debug) YLog.debug(
                "$instance -> findMethod ${first().className}|${first().methodName}", tag = tag
            )
        }
        return this
    }
}
