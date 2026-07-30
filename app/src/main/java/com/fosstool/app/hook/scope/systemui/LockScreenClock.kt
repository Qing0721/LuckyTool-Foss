package com.fosstool.app.hook.scope.systemui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import com.fosstool.app.hook.utils.sysui.ClockSwitchHelper
import com.fosstool.app.hook.utils.sysui.WeatherInfoParseHelper
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.A14
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK
import com.fosstool.app.utils.safeOf
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.buildOf
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.UnitType
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Field
import java.util.Calendar

@SuppressLint("DiscouragedApi")
private fun TextView.setClockRed(format: String, redMode: String) {
    if (redMode == "0" || redMode == "2") {
        if (redMode == "2") text = format
        return
    }
    val sp = SpannableStringBuilder(format)
    if (redMode == "1") {
        for (i in format.indices) {
            if (format[i] == '1') {
                val color = safeOf(Color.parseColor("#E62F2F")) {
                    val id = resources.getIdentifier(
                        "red_clock_hour_color", "color", context.packageName,
                    )
                    if (id != 0) context.getColor(id) else Color.parseColor("#E62F2F")
                }
                sp.setSpan(ForegroundColorSpan(color), i, i + 1, 34)
            }
        }
    }
    text = sp
}

private fun Class<*>.fieldAny(vararg names: String): Field? {
    for (n in names) {
        runCatching { return getDeclaredField(n).also { it.isAccessible = true } }
    }
    var cls: Class<*>? = this
    while (cls != null) {
        for (n in names) {
            runCatching { return cls.getDeclaredField(n).also { it.isAccessible = true } }
        }
        cls = cls.superclass
    }
    return declaredFields.firstOrNull { f ->
        names.any { n -> f.name.equals(n, true) || f.name.contains(n, true) }
    }?.also { it.isAccessible = true }
}

private fun Class<*>.textViewField(instance: Any?, vararg names: String): TextView? {
    (fieldAny(*names)?.get(instance) as? TextView)?.let { return it }
    return declaredFields.firstOrNull {
        TextView::class.java.isAssignableFrom(it.type) &&
            names.any { n -> it.name.contains(n, true) }
    }?.let {
        it.isAccessible = true
        it.get(instance) as? TextView
    }
}

class LockScreenClock : YukiBaseHooker() {

    companion object {
        @Volatile
        var redMode: String = "0"

        @Volatile
        var dualClock: Boolean = false
    }

    override fun onHook() {
        redMode = prefs(ModulePrefs).getString("lock_screen_clock_redone_mode", "0")
        dataChannel.wait<String>("lock_screen_clock_redone_mode") { redMode = it }
        dualClock = prefs(ModulePrefs).getBoolean("apply_lock_screen_dual_clock_redone", false)
        dataChannel.wait<Boolean>("apply_lock_screen_dual_clock_redone") { dualClock = it }

        if (redMode != "0") hookRedOneViaDexKit()
        hookSingleClock()
        hookRedTextClock()
        hookDualClock()
        if (SDK >= A13) loadHooker(HookRedDuanClock())
        else loadHooker(HookRedDuanClock12())
    }

    private fun hookRedOneViaDexKit() {
        if (redMode != "1" && redMode != "2") return
        runCatching {
            DexkitUtils.create(appInfo.sourceDir) { bridge ->
                bridge.findClass {
                    matcher {
                        addFieldForType(BooleanType.name)
                        usingStrings(
                            "ro.oplus.image.system_ext.brand",
                            "ro.oplus.image.system_ext.area"
                        )
                    }
                }.apply {
                    checkDataList("KeyGuardcLockRedMode Clazz")
                    if (isEmpty()) return@apply
                    val fieldData = findField {
                        matcher {
                            type = BooleanType.name
                            addReadMethod {
                                paramCount(1)
                                returnType(UnitType.name)
                                usingStrings("1")
                                usingNumbers(1)
                            }
                        }
                    }.firstOrNull() ?: return@apply
                    fieldData.declaredClassName.toClassOrNull(appClassLoader)
                        ?.field {
                            name = fieldData.fieldName
                            type = BooleanType
                        }
                        ?.ignored()
                        ?.get()
                        ?.set(redMode == "1")
                }
            }
        }
    }

    private fun hookSingleClock() {
        VariousClass(
            "com.oplusos.systemui.keyguard.clock.SingleClockView",
            "com.oplus.systemui.shared.clocks.SingleClockView",
            "com.oplus.systemui.keyguard.clock.SingleClockView",
        ).toClassOrNull(appClassLoader)?.let { c ->
            val paint: (XC_MethodHook.MethodHookParam) -> Unit = paint@{ param ->
                if (redMode == "0") return@paint
                val hourView = c.textViewField(param.thisObject, "mTimeHour", "TimeHour", "hour")
                    ?: return@paint
                val mHour = (c.fieldAny("mHour", "hour")?.get(param.thisObject) as? String)
                    ?.takeIf { it.isNotBlank() } ?: return@paint
                hourView.setClockRed(mHour, redMode)
            }
            c.declaredMethods
                .filter {
                    it.parameterCount == 0 &&
                        (it.name.contains("updateTime", true) ||
                            it.name.contains("StandardTime", true))
                }
                .forEach { m ->
                    runCatching {
                        XposedBridge.hookMethod(m, object : XC_MethodHook() {
                            override fun afterHookedMethod(param: MethodHookParam) {
                                paint(param)
                            }
                        })
                    }
                }
        }
    }

    private fun hookRedTextClock() {
        VariousClass(
            "com.oplusos.systemui.keyguard.clock.RedTextClock",
            "com.oplus.systemui.shared.clocks.RedTextClock",
            "com.oplus.systemui.keyguard.clock.RedTextClock",
        ).toClassOrNull(appClassLoader)?.let { c ->
            c.method { name = "onTimeChanged" }.ignored().hook {
                after {
                    if (redMode == "0") return@after
                    val ticker = c.fieldAny("mShouldRunTicker", "ShouldRunTicker")
                        ?.get(instance) as? Boolean ?: true
                    if (!ticker) return@after
                    val format = c.fieldAny("format", "mFormat")?.get(instance) as? String
                        ?: return@after
                    val mTime = c.fieldAny("mTime", "time")?.get(instance) as? Calendar
                        ?: return@after
                    val mTimeHour = instance as? TextView ?: return@after
                    val mHour = android.text.format.DateFormat.format(format, mTime) as String
                    mTimeHour.setClockRed(mHour, redMode)
                }
            }
        }
    }

    private fun hookDualClock() {
        val weatherInfoClazz = VariousClass(
            "com.oplus.systemui.keyguard.clock.WeatherInfoParseHelper\$WeatherInfo",
            "com.oplusos.systemui.keyguard.clock.WeatherInfoParseHelper\$WeatherInfo",
            "com.oplus.systemui.shared.clocks.WeatherInfoParseHelper\$WeatherInfo",
        ).toClassOrNull(appClassLoader)
        VariousClass(
            "com.oplusos.systemui.keyguard.clock.DualClockView",
            "com.oplus.systemui.shared.clocks.DualClockView",
            "com.oplus.systemui.keyguard.clock.DualClockView",
        ).toClassOrNull(appClassLoader)?.let { c ->
            c.declaredMethods
                .filter { m ->
                    weatherInfoClazz != null && m.parameterTypes.any { it == weatherInfoClazz } ||
                        m.name.contains("LocatedTime", true) ||
                        m.name.contains("ResidentTime", true)
                }
                .forEach { m ->
                    runCatching {
                        XposedBridge.hookMethod(m, object : XC_MethodHook() {
                            override fun afterHookedMethod(param: MethodHookParam) {
                                if (!dualClock || redMode == "0") return
                                val type = when {
                                    m.name.contains("Located", true) ||
                                        m.name.contains("Locate", true) -> "LocatedTime"
                                    m.name.contains("Resident", true) -> "ResidentTime"
                                    else -> return
                                }
                                when (type) {
                                    "LocatedTime" -> {
                                        val hourView = c.textViewField(
                                            param.thisObject,
                                            "mLocatedTimeHour", "LocatedTimeHour", "Locate",
                                        ) ?: return
                                        val info = c.fieldAny("mLocatedTimeInfo", "LocatedTimeInfo")
                                            ?.get(param.thisObject) ?: return
                                        val mHour = runCatching {
                                            XposedHelpers.callMethod(info, "getHour") as? String
                                        }.getOrNull() ?: return
                                        hourView.setClockRed(mHour, redMode)
                                    }
                                    "ResidentTime" -> {
                                        val hourView = c.textViewField(
                                            param.thisObject,
                                            "mResidentTimeHour", "ResidentTimeHour", "Resident",
                                        ) ?: return
                                        val info = c.fieldAny("mResidentTimeInfo", "ResidentTimeInfo")
                                            ?.get(param.thisObject) ?: return
                                        val mHour = runCatching {
                                            XposedHelpers.callMethod(info, "getHour") as? String
                                        }.getOrNull() ?: return
                                        hourView.setClockRed(mHour, redMode)
                                    }
                                }
                            }
                        })
                    }
                }
        }
    }

    private class HookRedDuanClock : YukiBaseHooker() {
        override fun onHook() {
            val timeInfoClazz = VariousClass(
                "com.oplus.systemui.keyguard.clock.WeatherInfoParseHelper\$TimeInfo",
                "com.oplusos.systemui.keyguard.clock.WeatherInfoParseHelper\$TimeInfo",
            ).toClassOrNull(appClassLoader)
            VariousClass(
                "com.oplusos.systemui.keyguard.clock.RedHorizontalDualClockView",
                "com.oplus.systemui.shared.clocks.RedHorizontalDualClockView",
            ).toClassOrNull(appClassLoader)?.let { c ->
                c.declaredMethods
                    .filter { m ->
                        (m.parameterCount == 3 && timeInfoClazz != null &&
                            m.parameterTypes.any { it == timeInfoClazz }) ||
                            m.name.contains("Locate", true) ||
                            m.name.contains("Resident", true)
                    }
                    .forEach { m ->
                        runCatching {
                            XposedBridge.hookMethod(m, object : XC_MethodHook() {
                                override fun afterHookedMethod(param: MethodHookParam) {
                                    if (!dualClock || redMode == "0") return
                                    val isLocate = m.name.contains("Locate", true)
                                    val hourView = c.textViewField(
                                        param.thisObject,
                                        if (isLocate) "mTvHorizontalLocateClockHour"
                                        else "mTvHorizontalResidentClockHour",
                                        if (isLocate) "Locate" else "Resident",
                                        "Hour",
                                    ) ?: return
                                    val infoArg = param.args.firstOrNull {
                                        it != null && it.javaClass.simpleName.contains("Time", true)
                                    } ?: param.args.getOrNull(0) ?: return
                                    val mHour = runCatching {
                                        XposedHelpers.callMethod(infoArg, "getHour") as? String
                                    }.getOrNull() ?: return
                                    hourView.setClockRed(mHour, redMode)
                                }
                            })
                        }
                    }
            }
        }
    }

    private class HookRedDuanClock12 : YukiBaseHooker() {
        override fun onHook() {
            VariousClass(
                "com.oplusos.systemui.keyguard.clock.RedHorizontalDualClockView",
                "com.oplus.systemui.shared.clocks.RedHorizontalDualClockView",
            ).toClassOrNull(appClassLoader)?.let { c ->
                c.method { name = "updateLocateTime" }.ignored().hook {
                    after {
                        if (!dualClock || redMode == "0") return@after
                        val mContext = c.fieldAny("mContext")?.get(instance) as? Context
                            ?: return@after
                        val hourView = c.textViewField(
                            instance, "mTvHorizontalLocateClockHour", "Locate", "Hour",
                        ) ?: return@after
                        val info = WeatherInfoParseHelper(appClassLoader).getLocalTimeInfo(mContext)
                            ?: return@after
                        val mHour = runCatching {
                            XposedHelpers.callMethod(info, "getHour") as? String
                        }.getOrNull() ?: return@after
                        hourView.setClockRed(mHour, redMode)
                    }
                }
                c.method { name = "updateResidentTime" }.ignored().hook {
                    after {
                        if (!dualClock || redMode == "0") return@after
                        val mContext = c.fieldAny("mContext")?.get(instance) as? Context
                            ?: return@after
                        val hourView = c.textViewField(
                            instance, "mTvHorizontalResidentClockHour", "Resident", "Hour",
                        ) ?: return@after
                        val info = ClockSwitchHelper(appClassLoader).let {
                            it.getInstance(mContext)?.let { its -> it.getResidentWeatherInfo(its) }
                        } ?: WeatherInfoParseHelper(appClassLoader).weatherInfoClazz.buildOf {
                            emptyParam()
                        }
                        val timeZone = runCatching {
                            XposedHelpers.callMethod(info, "getTimeZone") as? String
                        }.getOrNull() ?: "0.0"
                        val mResidentTimeInfo =
                            WeatherInfoParseHelper(appClassLoader).getResidentTimeInfo(
                                mContext, timeZone,
                            ) ?: return@after
                        val mHour = runCatching {
                            XposedHelpers.callMethod(mResidentTimeInfo, "getHour") as? String
                        }.getOrNull() ?: return@after
                        hourView.setClockRed(mHour, redMode)
                    }
                }
            }
        }
    }
}
