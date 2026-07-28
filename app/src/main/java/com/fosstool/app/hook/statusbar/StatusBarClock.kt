package com.fosstool.app.hook.statusbar

import android.content.Context
import android.graphics.Typeface
import android.os.Handler
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import com.fosstool.app.hook.utils.sysui.LunarHelperUtils
import com.fosstool.app.utils.A11
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK
import com.fosstool.app.utils.formatDate
import com.fosstool.app.utils.is24
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.Calendar
import java.util.Date
import java.util.Timer
import java.util.TimerTask

object StatusBarClock : YukiBaseHooker() {

    private val clockMode = prefs(ModulePrefs).getString("statusbar_clock_mode", "0")
    private var isYear = prefs(ModulePrefs).getBoolean("statusbar_clock_show_year", false)
    private var isMonth = prefs(ModulePrefs).getBoolean("statusbar_clock_show_month", false)
    private var isDay = prefs(ModulePrefs).getBoolean("statusbar_clock_show_day", false)
    private var isWeek = prefs(ModulePrefs).getBoolean("statusbar_clock_show_week", false)
    private var isPeriod = prefs(ModulePrefs).getBoolean("statusbar_clock_show_period", false)
    private var isDoubleHour =
        prefs(ModulePrefs).getBoolean("statusbar_clock_show_double_hour", false)
    private var isSecond = prefs(ModulePrefs).getBoolean("statusbar_clock_show_second", false)
    private var isHideSpace = prefs(ModulePrefs).getBoolean("statusbar_clock_hide_spaces", false)
    private val isDoubleRow = prefs(ModulePrefs).getBoolean("statusbar_clock_show_doublerow", false)

    private var clockAlignment =
        prefs(ModulePrefs).getString("statusbar_clock_text_alignment", "center")

    private var singleRowFontSize =
        prefs(ModulePrefs).getInt("statusbar_clock_singlerow_fontsize", 0)
    private var doubleRowFontSize =
        prefs(ModulePrefs).getInt("statusbar_clock_doublerow_fontsize", 0)

    private var customFormat =
        prefs(ModulePrefs).getString("statusbar_clock_custom_format", "HH:mm:ss")
    private var customFontsize = prefs(ModulePrefs).getInt("statusbar_clock_custom_fontsize", 0)

    private val userTypeface = prefs(ModulePrefs).getBoolean("statusbar_clock_user_typeface", false)
    private val boldTypeface =
        prefs(ModulePrefs).getBoolean("statusbar_clock_bold_typeface", false) ||
            prefs(ModulePrefs).getBoolean("statusbar_clock_use_bold_font_style", false)
    private var clockMinWidthDp = prefs(ModulePrefs).getInt("statusbar_clock_custom_minimum_width", 0)
    private val customPadding = prefs(ModulePrefs).getBoolean("statusbar_clock_custom_padding", false)
    private var padTop = prefs(ModulePrefs).getInt("statusbar_clock_custom_top_padding", 0)
    private var padBottom = prefs(ModulePrefs).getInt("statusbar_clock_custom_bottom_padding", 0)
    private var padLeft = prefs(ModulePrefs).getInt("statusbar_clock_custom_left_padding", 0)
    private var padRight = prefs(ModulePrefs).getInt("statusbar_clock_custom_right_padding", 0)

    private var nowLunar: String? = null
    private var nowTime: Date? = null
    private var newline = ""

    override fun onHook() {
        if (clockMode.isBlank() || clockMode == "0") return
        dataChannel.wait<String>("statusbar_clock_text_alignment") { clockAlignment = it }
        dataChannel.wait<String>("statusbar_clock_custom_format") { customFormat = it }
        dataChannel.wait<Int>("statusbar_clock_custom_fontsize") { customFontsize = it }
        dataChannel.wait<Int>("statusbar_clock_singlerow_fontsize") { singleRowFontSize = it }
        dataChannel.wait<Int>("statusbar_clock_doublerow_fontsize") { doubleRowFontSize = it }
        dataChannel.wait<Int>("statusbar_clock_custom_minimum_width") { clockMinWidthDp = it }
        dataChannel.wait<Int>("statusbar_clock_custom_top_padding") { padTop = it }
        dataChannel.wait<Int>("statusbar_clock_custom_bottom_padding") { padBottom = it }
        dataChannel.wait<Int>("statusbar_clock_custom_left_padding") { padLeft = it }
        dataChannel.wait<Int>("statusbar_clock_custom_right_padding") { padRight = it }
        dataChannel.wait<Boolean>("statusbar_clock_show_year") { isYear = it }
        dataChannel.wait<Boolean>("statusbar_clock_show_month") { isMonth = it }
        dataChannel.wait<Boolean>("statusbar_clock_show_day") { isDay = it }
        dataChannel.wait<Boolean>("statusbar_clock_show_week") { isWeek = it }
        dataChannel.wait<Boolean>("statusbar_clock_show_period") { isPeriod = it }
        dataChannel.wait<Boolean>("statusbar_clock_show_double_hour") { isDoubleHour = it }
        dataChannel.wait<Boolean>("statusbar_clock_show_second") { isSecond = it }
        dataChannel.wait<Boolean>("statusbar_clock_hide_spaces") { isHideSpace = it }
        var context: Context? = null

        val clockCls = VariousClass(
            "com.android.systemui.statusbar.policy.Clock",
            "com.oplus.systemui.statusbar.policy.Clock",
            "com.oplusos.systemui.statusbar.policy.Clock",
        ).toClassOrNull(appClassLoader)
        clockCls?.declaredConstructors?.firstOrNull { it.parameterCount == 3 }?.let { ctor ->
            runCatching {
                XposedBridge.hookMethod(ctor, object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        context = param.args.getOrNull(0) as? Context
                        val clockView = param.thisObject as? TextView ?: return
                        val resName = runCatching {
                            clockView.resources.getResourceEntryName(clockView.id)
                        }.getOrNull()
                        if (resName != null && resName != "clock" && !resName.contains("clock")) return
                        val d: Method = runCatching {
                            clockView.javaClass.superclass.getDeclaredMethod("updateClock")
                        }.getOrElse {
                            clockView.javaClass.getDeclaredMethod("updateClock")
                        }
                        val r = Runnable {
                            d.isAccessible = true
                            d.invoke(clockView)
                        }

                        class T : TimerTask() {
                            override fun run() {
                                Handler(clockView.context.mainLooper).post(r)
                            }
                        }
                        Timer().scheduleAtFixedRate(T(), 1000 - System.currentTimeMillis() % 1000, 1000)
                    }
                })
            }
        }
        clockCls?.method { name = "getSmallTime" }?.ignored()?.hook {
            after {
                val tv = instance as? TextView ?: return@after
                val resName = runCatching {
                    tv.resources.getResourceEntryName(tv.id)
                }.getOrNull()
                if (resName != null && resName != "clock" && !resName.contains("clock")) return@after
                tv.initView()
                val mCalendar = clockCls.findField("mCalendar")?.get(instance) as? Calendar
                nowTime = mCalendar?.time ?: Date()
                val ctx = context ?: tv.context
                if (clockMode == "1") result = getDate(ctx) + newline + getTime(ctx)
                else if (clockMode == "2") {
                    initLunar(ctx)
                    val t = nowTime ?: return@after
                    result = formatDate(getFormat(customFormat, t, nowLunar), t)
                }
            }
        }

        VariousClass(
            "com.oplusos.systemui.statusbar.widget.StatClock",
            "com.oplus.systemui.statusbar.widget.StatClock",
        ).toClassOrNull(appClassLoader)?.let { cls ->
            val configMethodName = if (SDK == A11) "onConfigChanged" else "onConfigurationChanged"
            cls.method { name = configMethodName }.ignored().hook { intercept() }
            cls.method { name = "getSmallTime" }.ignored().hook {
                after {
                    val tv = instance as? TextView ?: return@after
                    tv.initView()
                    val mCalendar = cls.findField("mCalendar")?.get(instance) as? Calendar
                    nowTime = mCalendar?.time ?: Date()
                    val ctx = context ?: tv.context
                    if (clockMode == "1") result = getDate(ctx) + newline + getTime(ctx)
                    else if (clockMode == "2") {
                        initLunar(ctx)
                        val t = nowTime ?: return@after
                        result = formatDate(getFormat(customFormat, t, nowLunar), t)
                    }
                }
            }
        }
    }

    private fun initLunar(context: Context) {
        val instance = LunarHelperUtils(appClassLoader).buildInstance(context)
        nowLunar =
            LunarHelperUtils(appClassLoader).getDateToString(instance, System.currentTimeMillis())
    }

    private fun TextView.initView() {
        if (userTypeface) typeface = if (boldTypeface) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        if (clockMode == "1") {
            isSingleLine = !isDoubleRow
            if (isDoubleRow) {
                newline = "\n"
                if (doubleRowFontSize != 0) {
                    setTextSize(TypedValue.COMPLEX_UNIT_DIP, doubleRowFontSize.toFloat())
                    setLineSpacing(0F, 0.8F)
                }
            } else {
                if (singleRowFontSize != 0) {
                    setTextSize(TypedValue.COMPLEX_UNIT_DIP, singleRowFontSize.toFloat())
                }
            }
        } else if (clockMode == "2") {
            val formatList =
                customFormat.takeIf { e -> e.isNotBlank() && e.contains("\n") }?.split("\n")
                    ?.toMutableList() ?: mutableListOf("0")
            formatList.removeIf { it.isBlank() }
            val rows = formatList.size
            isSingleLine = rows == 1
            if (customFontsize != 0) {
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, customFontsize.toFloat())
            }
            if (rows != 1) setLineSpacing(0F, 0.8F)
        }
        gravity = if (isSingleLine) Gravity.CENTER else when (clockAlignment) {
            "left" -> Gravity.START or Gravity.CENTER
            "center" -> Gravity.CENTER
            "right" -> Gravity.END or Gravity.CENTER
            else -> Gravity.CENTER
        }
        if (clockMinWidthDp > 0) {
            this.minimumWidth = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, clockMinWidthDp.toFloat(), resources.displayMetrics
            ).toInt()
        }
        if (customPadding) {
            val metrics = resources.displayMetrics
            fun dpToPx(v: Int) =
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), metrics).toInt()
            setPadding(
                if (padLeft != 0) dpToPx(padLeft) else getPaddingLeft(),
                if (padTop != 0) dpToPx(padTop) else getPaddingTop(),
                if (padRight != 0) dpToPx(padRight) else getPaddingRight(),
                if (padBottom != 0) dpToPx(padBottom) else getPaddingBottom()
            )
        }
    }

    private fun getFormat(format: String, nowTime: Date, nowLunar: String?): String {
        var finalFormat: String = format
        if (finalFormat.contains("NNNN")) finalFormat = finalFormat.replace("NNNN", nowLunar!!)
        if (finalFormat.contains("NNN")) finalFormat = finalFormat.replace(
            "NNN", nowLunar!!.substring(2, nowLunar.length)
        )
        if (finalFormat.contains("NN")) finalFormat = finalFormat.replace(
            "NN", nowLunar!!.substring(4, nowLunar.length)
        )
        if (finalFormat.contains("N")) {
            val startInt = if (nowLunar!!.length > 8) 7 else 6
            finalFormat = finalFormat.replace("N", nowLunar.substring(startInt, nowLunar.length))
        }
        if (finalFormat.contains("dddd")) finalFormat = finalFormat.replace("dddd", "dd号")
        if (finalFormat.contains("ddd")) finalFormat = finalFormat.replace("ddd", "d号")
        if (finalFormat.contains("FF")) finalFormat = finalFormat.replace("FF", getPeriod(nowTime))
        if (finalFormat.contains("GG")) finalFormat =
            finalFormat.replace("GG", getDoubleHour(nowTime))
        return finalFormat
    }

    private fun getPeriod(nowTime: Date): String {
        return when (formatDate("HH", nowTime)) {
            "00", "01", "02", "03", "04", "05" -> {
                "凌晨"
            }

            "06", "07", "08", "09", "10", "11" -> {
                "上午"
            }

            "12" -> {
                "中午"
            }

            "13", "14", "15", "16", "17" -> {
                "下午"
            }

            "18" -> {
                "傍晚"
            }

            "19", "20", "21", "22", "23" -> {
                "晚上"
            }

            else -> ""
        }
    }

    private fun getDoubleHour(nowTime: Date): String {
        return when (formatDate("HH", nowTime)) {
            "23", "00" -> {
                "子时"
            }

            "01", "02" -> {
                "丑时"
            }

            "03", "04" -> {
                "寅时"
            }

            "05", "06" -> {
                "卯时"
            }

            "07", "08" -> {
                "辰时"
            }

            "09", "10" -> {
                "巳时"
            }

            "11", "12" -> {
                "午时"
            }

            "13", "14" -> {
                "未时"
            }

            "15", "16" -> {
                "申时"
            }

            "17", "18" -> {
                "酉时"
            }

            "19", "20" -> {
                "戌时"
            }

            "21", "22" -> {
                "亥时"
            }

            else -> ""
        }
    }

    private fun getDate(context: Context): String {
        var dateFormat = ""
        if (isZh(context)) {
            if (isYear) dateFormat += "YY年"
            if (isMonth) dateFormat += "M月"
            if (isDay) dateFormat += "d日"
            if (isWeek) dateFormat += "E"
            if (!isHideSpace && !isDoubleRow) dateFormat += " "
        } else {
            if (isWeek) dateFormat += "E"
            if (!isHideSpace && !isDoubleRow) dateFormat += " "
            if (isMonth) {
                dateFormat += "M"
                if (isDay || isYear) dateFormat += "/"
            }
            if (isDay) {
                dateFormat += "d"
                if (isYear) dateFormat += "/"
            }
            if (isYear) {
                dateFormat += "YY"
            }
            if (!isHideSpace && !isDoubleRow) dateFormat += " "
        }
        return formatDate(dateFormat, nowTime!!)
    }

    private fun getTime(context: Context): String {
        var period: String
        var doubleHour: String
        var timeFormat = ""
        timeFormat += if (context.is24) "HH:mm" else "hh:mm"
        if (isSecond) timeFormat += ":ss"
        timeFormat = formatDate(timeFormat, nowTime!!)
        if (isPeriod) {
            if (isZh(context)) {
                period = getPeriod(nowTime!!)
                if (!isHideSpace) period += " "
                timeFormat = period + timeFormat
            } else {
                period = " " + formatDate("a", nowTime!!)
                timeFormat += period
            }
        }
        if (isDoubleHour) {
            doubleHour = getDoubleHour(nowTime!!)
            if (!isHideSpace) doubleHour = "$doubleHour "
            timeFormat = doubleHour + timeFormat
        }
        return timeFormat
    }

    private fun isZh(context: Context): Boolean {
        val locale = context.resources.configuration.locales[0]
        val language = locale.language
        return language.endsWith("zh")
    }

    private fun Class<*>.findField(name: String): Field? {
        var c: Class<*>? = this
        while (c != null && c != Any::class.java) {
            c.declaredFields.firstOrNull { it.name == name }?.let { return it.apply { isAccessible = true } }
            c = c.superclass
        }
        return null
    }
}
