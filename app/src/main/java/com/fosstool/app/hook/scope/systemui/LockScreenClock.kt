package com.fosstool.app.hook.scope.systemui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.buildOf
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.hook.utils.sysui.ClockSwitchHelper
import com.fosstool.app.hook.utils.sysui.WeatherInfoParseHelper
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.A14
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK
import com.fosstool.app.utils.safeOf
import java.util.Calendar

object LockScreenClock : YukiBaseHooker() {

    lateinit var redMode: String
    var dualClock = false

    override fun onHook() {
        redMode = prefs(ModulePrefs).getString("lock_screen_clock_redone_mode", "0")
        dataChannel.wait<String>("lock_screen_clock_redone_mode") { redMode = it }
        dualClock = prefs(ModulePrefs).getBoolean("apply_lock_screen_dual_clock_redone", false)
        dataChannel.wait<Boolean>("apply_lock_screen_dual_clock_redone") { dualClock = it }

        VariousClass(
            "com.oplusos.systemui.keyguard.clock.SingleClockView",
            "com.oplus.systemui.shared.clocks.SingleClockView"
        ).toClass().apply {
            method { name = "updateStandardTime" }.hook {
                after {
                    if (redMode == "0") return@after
                    val mTimeHour = field { name = "mTimeHour" }.get(instance).cast<TextView>()
                        ?: return@after
                    val mHour = field { name = "mHour" }.get(instance).string()
                        .takeIf { e -> e.isNotBlank() } ?: return@after
                    mTimeHour.setClockRed(mHour, redMode)
                }
            }
        }
        VariousClass(
            "com.oplusos.systemui.keyguard.clock.RedTextClock",
            "com.oplus.systemui.shared.clocks.RedTextClock"
        ).toClass().apply {
            method { name = "onTimeChanged" }.hook {
                after {
                    if (redMode == "0") return@after
                    val mShouldRunTicker =
                        field { name = "mShouldRunTicker" }.get(instance).boolean()
                    if (!mShouldRunTicker) return@after
                    val format = field { name = "format" }.get(instance).string()
                    val mTime =
                        field { name = "mTime" }.get(instance).cast<Calendar>() ?: return@after
                    val mTimeHour = instance<TextView>()
                    val mHour = android.text.format.DateFormat.format(format, mTime) as String
                    mTimeHour.setClockRed(mHour, redMode)
                }
            }
        }
        val weatherInfoClazz =
            if (SDK >= A14) "com.oplus.systemui.keyguard.clock.WeatherInfoParseHelper\$WeatherInfo".toClass()
            else "com.oplusos.systemui.keyguard.clock.WeatherInfoParseHelper\$WeatherInfo".toClass()
        VariousClass(
            "com.oplusos.systemui.keyguard.clock.DualClockView",
            "com.oplus.systemui.shared.clocks.DualClockView"
        ).toClass().apply {
            method { param { it.contains(weatherInfoClazz) } }.hookAll {
                after {
                    if (!dualClock) return@after
                    val type: String = method.name.let { s ->
                        if (s.contains("updateLocatedTime")) "LocatedTime"
                        else if (s.contains("updateResidentTime")) "ResidentTime"
                        else return@after
                    }
                    when (type) {
                        "LocatedTime" -> {
                            val mLocatedTimeHour =
                                field { name = "mLocatedTimeHour" }.get(instance)
                                    .cast<TextView>()
                                    ?: return@after
                            val mLocatedTimeInfo =
                                field { name = "mLocatedTimeInfo" }.get(instance).any()
                                    ?: return@after
                            val mHour =
                                mLocatedTimeInfo.current().method { name = "getHour" }
                                    .invoke<String>() ?: return@after
                            mLocatedTimeHour.setClockRed(mHour, redMode)
                        }

                        "ResidentTime" -> {
                            val mResidentTimeHour =
                                field { name = "mResidentTimeHour" }.get(instance)
                                    .cast<TextView>()
                                    ?: return@after
                            val mResidentTimeInfo =
                                field { name = "mResidentTimeInfo" }.get(instance).any()
                                    ?: return@after
                            val mHour =
                                mResidentTimeInfo.current().method { name = "getHour" }
                                    .invoke<String>() ?: return@after
                            mResidentTimeHour.setClockRed(mHour, redMode)
                        }
                    }
                }
            }
        }

        if (SDK >= A14) return
        else if (SDK == A13) loadHooker(HookRedDuanClock)
        else loadHooker(HookRedDuanClock12)
    }

    @SuppressLint("DiscouragedApi")
    private fun TextView.setClockRed(format: String, redMode: String) {
        val sp = SpannableStringBuilder(format)
        if (redMode == "1") {
            for (i in format.indices) {
                if (format[i].toString() == "1") {
                    val color = safeOf(Color.parseColor("#c41442")) {
                        context.getColor(
                            resources.getIdentifier(
                                "red_clock_hour_color", "color", packageName
                            )
                        )
                    }
                    sp.setSpan(ForegroundColorSpan(color), i, i + 1, 34)
                }
            }
        }
        text = sp
    }

    private object HookRedDuanClock : YukiBaseHooker() {
        override fun onHook() {
            val timeInfoClazz =
                if (SDK >= A14) "com.oplus.systemui.keyguard.clock.WeatherInfoParseHelper\$TimeInfo".toClass()
                else "com.oplusos.systemui.keyguard.clock.WeatherInfoParseHelper\$TimeInfo".toClass()
            VariousClass(
                "com.oplusos.systemui.keyguard.clock.RedHorizontalDualClockView",
                "com.oplus.systemui.shared.clocks.RedHorizontalDualClockView"
            ).toClass().apply {
                method { param { it.contains(timeInfoClazz) };paramCount = 3 }.hookAll {
                    after {
                        if (!dualClock) return@after
                        val type: String = method.name.let { s ->
                            if (s.contains("updateLocateTime")) "LocateTime"
                            else if (s.contains("updateResidentTime")) "ResidentTime"
                            else return@after
                        }
                        val view = args().first().any() ?: return@after
                        when (type) {
                            "LocateTime" -> {
                                val mLocatedTimeHour =
                                    view.current()
                                        .field { name = "mTvHorizontalLocateClockHour" }
                                        .cast<TextView>() ?: return@after
                                val mLocatedTimeInfo = args().last().any() ?: return@after
                                val mHour =
                                    mLocatedTimeInfo.current().method { name = "getHour" }
                                        .invoke<String>() ?: return@after
                                mLocatedTimeHour.setClockRed(mHour, redMode)
                            }

                            "ResidentTime" -> {
                                val mResidentTimeHour =
                                    view.current()
                                        .field { name = "mTvHorizontalResidentClockHour" }
                                        .cast<TextView>() ?: return@after
                                val mResidentTimeInfo = args().last().any() ?: return@after
                                val mHour =
                                    mResidentTimeInfo.current().method { name = "getHour" }
                                        .invoke<String>() ?: return@after
                                mResidentTimeHour.setClockRed(mHour, redMode)
                            }
                        }
                    }
                }
            }
        }
    }

    private object HookRedDuanClock12 : YukiBaseHooker() {
        override fun onHook() {
            "com.oplusos.systemui.keyguard.clock.RedHorizontalDualClockView".toClass().apply {
                method { name = "updateLocateTime" }.hook {
                    after {
                        if (!dualClock) return@after
                        val mContext = field { name = "mContext" }.get(instance).cast<Context>()
                            ?: return@after
                        val mLocatedTimeHour = field { name = "mTvHorizontalLocateClockHour" }
                            .get(instance).cast<TextView>() ?: return@after
                        val mLocatedTimeInfo =
                            WeatherInfoParseHelper(appClassLoader).getLocalTimeInfo(mContext)
                                ?: return@after
                        val mHour =
                            mLocatedTimeInfo.current().method { name = "getHour" }.invoke<String>()
                                ?: return@after
                        mLocatedTimeHour.setClockRed(mHour, redMode)
                    }
                }
                method { name = "updateResidentTime" }.hook {
                    after {
                        if (!dualClock) return@after
                        val mContext = field { name = "mContext" }.get(instance).cast<Context>()
                            ?: return@after
                        val mResidentTimeHour = field { name = "mTvHorizontalResidentClockHour" }
                            .get(instance).cast<TextView>() ?: return@after
                        val info = ClockSwitchHelper(appClassLoader).let {
                            it.getInstance(mContext)?.let { its -> it.getResidentWeatherInfo(its) }
                        } ?: WeatherInfoParseHelper(appClassLoader).weatherInfoClazz.buildOf {
                            emptyParam()
                        }
                        val timeZone =
                            info?.current()?.method { name = "getTimeZone" }?.invoke<String>()
                                ?: "0.0"
                        val mResidentTimeInfo =
                            WeatherInfoParseHelper(appClassLoader).getResidentTimeInfo(
                                mContext, timeZone
                            ) ?: return@after
                        val mHour =
                            mResidentTimeInfo.current().method { name = "getHour" }.invoke<String>()
                                ?: return@after
                        mResidentTimeHour.setClockRed(mHour, redMode)
                    }
                }
            }
        }
    }
}
