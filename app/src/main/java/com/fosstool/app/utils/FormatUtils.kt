package com.fosstool.app.utils

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Formatter
import java.util.Locale
import java.util.regex.Pattern

fun formatDate(format: String): String {
    return formatDate(format, null, null)
}

fun formatDate(format: String, param: Any): String {
    return formatDate(format, param, null)
}

fun formatDate(format: String, param: Any?, locale: Locale?): String {
    return SimpleDateFormat(format, locale ?: Locale.getDefault()).format(param ?: Date())
}

fun formatDouble(format: String, param: Any): Double {
    return Formatter().format(format, param).toString().toDoubleOrNull() ?: 0.0
}

fun formatSpace(string: String): String {
    val pattern = Pattern.compile("\\p{Alpha}")
    val matcher = pattern.matcher(string)
    if (!matcher.find()) return string
    return string.substring(matcher.start())
}

fun formatDataSize(str: String): String {
    val int = str.toFloatOrNull() ?: return str
    return if (int >= (1024 * 1024 * 1024)) {
        DecimalFormat("0.00").format(int / (1024 * 1024 * 1024)).toString() + " GB"
    } else if (int >= (1024 * 1024)) {
        DecimalFormat("0.00").format(int / (1024 * 1024)).toString() + " MB"
    } else if (int >= (1024)) {
        DecimalFormat("0.00").format(int / (1024)).toString() + " KB"
    } else "$int B"
}

val CharSequence.filterNumber get() = this.replace("\\D".toRegex(), "")

val String.replaceSpace get() = this.replace(" ", "")

val String.replaceBlankLine: String
    get() {
        val listString = this.replaceSpace
        if (listString.contains("\n").not()) return listString
        val formatList = listString.split("\n").toMutableList().apply {
            removeIf { it.isBlank() }
        }
        var finalString = ""
        formatList.forEachIndexed { index, s ->
            finalString += s
            if (formatList.lastIndex != index) finalString += "\n"
        }
        return finalString
    }
