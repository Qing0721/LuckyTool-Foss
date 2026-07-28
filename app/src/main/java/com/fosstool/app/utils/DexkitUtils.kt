package com.fosstool.app.utils

import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe

import com.fosstool.app.BuildConfig
import com.highcapable.yukihookapi.hook.log.YLog
import org.luckypray.dexkit.DexKitBridge
import org.luckypray.dexkit.result.ClassData
import org.luckypray.dexkit.result.ClassDataList
import org.luckypray.dexkit.result.MethodData
import org.luckypray.dexkit.result.MethodDataList

@Suppress("MemberVisibilityCanBePrivate")
object DexkitUtils {
    const val tag = "LuckyTool"

    const val DEBUG_DEXKIT = false

    val debug = BuildConfig.DEBUG || DEBUG_DEXKIT

    fun create(appPath: String, result: (DexKitBridge) -> Unit) {
        runCatching {
            System.loadLibrary("dexkit")
            DexKitBridge.create(appPath)?.use { result(it) }
        }.onFailure { YLog.error("DexkitUtils.create failed: $appPath", it, tag = tag) }
    }

    fun ClassDataList.checkDataList(instance: String, onlyOne: Boolean = true): ClassDataList {
        when {
            isEmpty() -> if (debug) YLog.error("$instance -> findClass isNullOrEmpty", tag = tag)
            size > 1 && onlyOne -> {
                var find = ""
                forEach { find += "[${it.name}]" }
                if (debug) YLog.error("$instance -> findClass size ($size) -> $find", tag = tag)
            }
            size == 1 -> if (debug) YLog.debug(
                "$instance -> findClass ${first().name}", tag = tag,
            )
        }
        return this
    }

    fun MethodDataList.checkDataList(instance: String, onlyOne: Boolean = true): MethodDataList {
        when {
            isEmpty() -> if (debug) YLog.error("$instance -> findMethod isNullOrEmpty", tag = tag)
            size > 1 && onlyOne -> {
                var find = ""
                forEach { find += "[${it.className}|${it.methodName}]" }
                if (debug) YLog.error("$instance -> findMethod size ($size) -> $find", tag = tag)
            }
            size == 1 -> if (debug) YLog.debug(
                "$instance -> findMethod ${first().className}|${first().methodName}", tag = tag,
            )
        }
        return this
    }

    fun ClassDataList.firstOrNullSafe(): ClassData? = if (isEmpty()) null else first()

    fun MethodDataList.firstOrNullSafe(): MethodData? = if (isEmpty()) null else first()

    inline fun ClassDataList.useFirst(
        instance: String,
        onlyOne: Boolean = true,
        block: (ClassData) -> Unit,
    ) {
        checkDataList(instance, onlyOne)
        firstOrNullSafe()?.let(block)
    }

    inline fun MethodDataList.useFirst(
        instance: String,
        onlyOne: Boolean = true,
        block: (MethodData) -> Unit,
    ) {
        checkDataList(instance, onlyOne)
        firstOrNullSafe()?.let(block)
    }

    inline fun ClassDataList.useEach(
        instance: String,
        block: (ClassData) -> Unit,
    ) {
        checkDataList(instance, onlyOne = false)
        forEach(block)
    }

    inline fun MethodDataList.useEach(
        instance: String,
        block: (MethodData) -> Unit,
    ) {
        checkDataList(instance, onlyOne = false)
        forEach(block)
    }
}
