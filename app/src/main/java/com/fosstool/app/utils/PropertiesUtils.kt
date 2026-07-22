package com.fosstool.app.utils

import android.text.TextUtils
import java.util.Properties

fun Properties.getStringProperty(key: String): String? {
    return if (TextUtils.isEmpty(key)) {
        null
    } else getProperty(key)
}

fun Properties.getStringProperty(key: String, def: String? = ""): String? {
    return if (TextUtils.isEmpty(key)) {
        null
    } else if (getStringProperty(key) == null) {
        def
    } else getStringProperty(key)
}

fun Properties.getIntProperty(key: String): Int {
    val stringProperty = getStringProperty(key)
    return if (TextUtils.isEmpty(stringProperty)) {
        -1
    } else try {
        stringProperty!!.trim { it <= ' ' }.toInt()
    } catch (e: Exception) {
        -1
    }
}

fun Properties.getBooleanProperty(key: String): Boolean {
    val stringProperty = getStringProperty(key)
    return if (TextUtils.isEmpty(stringProperty)) {
        false
    } else try {
        java.lang.Boolean.parseBoolean(stringProperty)
    } catch (e: Exception) {
        false
    }
}
