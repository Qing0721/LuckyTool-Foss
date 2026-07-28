package com.fosstool.app.hook.utils.sysui

import android.content.Context
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.factory.buildOf
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.IntType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LunarHelperUtils(val classLoader: ClassLoader?) {

    val clazz: Class<*>? = VariousClass(
        "com.oplusos.systemui.keyguard.clock.LunarHelper",
        "com.oplus.systemui.keyguard.clock.LunarHelper"
    ).getOrNull(classLoader)

    fun buildInstance(context: Context): Any? = runCatching {
        clazz?.buildOf(context) { param(ContextClass) }
    }.getOrNull()

    fun generateLunarDate(style: Int = 0): String = runCatching {
        val solar = solarClazz ?: return@runCatching ""
        val now = Date(System.currentTimeMillis())
        val chinese = solar.method {
            name = "SunDateToChineseDate"
            param(IntType, IntType, IntType)
        }.get().invoke<IntArray>(
            fmt(now, "yyyy").toInt(), fmt(now, "MM").toInt(), fmt(now, "dd").toInt()
        ) ?: return@runCatching ""
        if (chinese.size < 3) return@runCatching ""

        val leapMonth = solar.method {
            name = "GetChLeapMonth"
            param(IntType)
        }.get().invoke<Int>(chinese[0]) ?: 0

        var month = chinese[1]
        if (leapMonth in 1..12) {
            val next = leapMonth + 1
            if (next == month) month = leapMonth + 12 else if (month > next) month--
        }
        chinese[1] = month

        var ganZhi = ""
        runCatching {
            val i = chinese[0] - 1864
            ganZhi = HEAVENLY_STEMS[i % 10] + EARTHLY_BRANCHES[i % 12]
        }

        var zodiac = ""
        runCatching { zodiac = ZODIACS[(chinese[0] - 4) % 12] }

        var monthStr = ""
        runCatching {
            monthStr = if (leapMonth in 1..12 && chinese[1] - 12 == leapMonth) {
                LEAP_AND_MONTH[0] + monthName(leapMonth)
            } else {
                monthName(chinese[1])
            }
        }

        var dayStr = ""
        runCatching { dayStr = LUNAR_DAYS[chinese[2] - 1] }

        when (style) {
            1 -> dayStr
            2 -> monthStr + dayStr
            3 -> zodiac + monthStr + dayStr
            else -> ganZhi + zodiac + monthStr + dayStr
        }
    }.getOrDefault("")

    @Suppress("UNUSED_PARAMETER")
    fun getDateToString(instance: Any?, time: Long): String? = generateLunarDate(0)

    private val solarClazz: Class<*>?
        get() = "com.oplus.util.OplusChineseDateAndSolarDate".toClassOrNull(classLoader)
            ?: runCatching {
                Class.forName("com.oplus.util.OplusChineseDateAndSolarDate")
            }.getOrNull()

    private fun fmt(date: Date, pattern: String): String =
        SimpleDateFormat(pattern, Locale.getDefault()).format(date)

    private fun monthName(month: Int): String = runCatching {
        val i = if (month > 12) month - 12 else month
        LUNAR_MONTHS[i] + LEAP_AND_MONTH[1]
    }.getOrDefault("")

    private companion object {

        val LEAP_AND_MONTH = arrayOf("闰", "月")

        val ZODIACS = arrayOf(
            "鼠年", "牛年", "虎年", "兔年", "龙年", "蛇年",
            "马年", "羊年", "猴年", "鸡年", "狗年", "猪年"
        )

        val HEAVENLY_STEMS = arrayOf("甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸")

        val EARTHLY_BRANCHES = arrayOf(
            "子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"
        )

        val LUNAR_MONTHS = arrayOf(
            "月", "正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "腊"
        )

        val LUNAR_DAYS = arrayOf(
            "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
            "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
            "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
        )
    }
}
