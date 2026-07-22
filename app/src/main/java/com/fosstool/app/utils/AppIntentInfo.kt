package com.fosstool.app.utils

enum class IntentType(val serialName: String) {
    SINGLE_SHARE("SINGLE_SHARE"),
    MULTI_SHARE("MULTI_SHARE"),
    PROCESS_TEXT("PROCESS_TEXT"),
    CONTENT("CONTENT"),
    FILE("FILE"),
    HTTP_LINK("HTTP_LINK"),
    HTTPS_LINK("HTTPS_LINK"),
    UNKNOWN("UNKNOWN");

    companion object {
        val SHARE_GROUP: List<IntentType> = listOf(SINGLE_SHARE, MULTI_SHARE)
        val TEXT_GROUP: List<IntentType> = listOf(PROCESS_TEXT)
        val OPEN_WITH_GROUP: List<IntentType> = listOf(CONTENT, FILE)
        val BROWSER_GROUP: List<IntentType> = listOf(HTTP_LINK, HTTPS_LINK)

        fun fromSerialName(name: String): IntentType =
            values().firstOrNull { it.serialName == name } ?: UNKNOWN
    }
}

data class IntentAppUpdate(val packageName: String, val enabled: Boolean) : java.io.Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}

data class AppIntentInfo(
    val name: String,
    val packName: String,
    val action: String,
    val activity: String,
    val type: IntentType,
) {
    fun toJson(): String = buildString {
        append('{')
        append("\"name\":").append(quote(name)).append(',')
        append("\"packName\":").append(quote(packName)).append(',')
        append("\"action\":").append(quote(action)).append(',')
        append("\"activity\":").append(quote(activity)).append(',')
        append("\"type\":").append(quote(type.serialName))
        append('}')
    }

    companion object {
        fun fromJson(json: String): AppIntentInfo? = runCatching {
            val o = org.json.JSONObject(json)
            AppIntentInfo(
                name = o.optString("name"),
                packName = o.optString("packName"),
                action = o.optString("action"),
                activity = o.optString("activity"),
                type = IntentType.fromSerialName(o.optString("type")),
            )
        }.getOrNull()

        private fun quote(s: String): String {
            val sb = StringBuilder("\"")
            for (c in s) when (c) {
                '"' -> sb.append("\\\"")
                '\\' -> sb.append("\\\\")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                else -> {
                    if (c.code < 0x20) sb.append(String.format("\\u%04x", c.code))
                    else sb.append(c)
                }
            }
            sb.append('"')
            return sb.toString()
        }
    }
}
