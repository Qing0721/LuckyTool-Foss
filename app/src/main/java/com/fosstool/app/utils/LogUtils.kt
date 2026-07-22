package com.fosstool.app.utils

import android.util.Log
import com.fosstool.app.BuildConfig

@Suppress("MemberVisibilityCanBePrivate")
object LogUtils {

    private const val globalTag = "LuckyTool"
    var enable = BuildConfig.DEBUG

    fun d(tag: String, method: String, msg: String) {
        if (enable) Log.d(globalTag, "$tag: $method -> $msg")
    }

    fun e(tag: String, method: String, msg: String) {
        if (enable) Log.e(globalTag, "$tag: $method -> $msg")
    }

    fun i(tag: String, method: String, msg: String) {
        if (enable) Log.i(globalTag, "$tag: $method -> $msg")
    }

    fun v(tag: String, method: String, msg: String) {
        if (enable) Log.v(globalTag, "$tag: $method -> $msg")
    }

    fun w(tag: String, method: String, msg: String) {
        if (enable) Log.w(globalTag, "$tag: $method -> $msg")
    }
}
