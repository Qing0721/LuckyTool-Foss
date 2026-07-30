package com.fosstool.app.hook.statusbar

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.net.TrafficStats
import android.os.Handler
import android.os.Message
import android.os.SystemClock
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams
import android.widget.TextView
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK
import com.fosstool.app.utils.dp
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Field
import java.util.Locale

object StatusBarNetWorkSpeed : YukiBaseHooker() {

    private var numberMarginBottom: Int = 0

    private var unitMarginTop: Int = 0

    private var lastRxBytes: Long = 0L
    private var lastTxBytes: Long = 0L
    private var lastSampleNanos: Long = 0L
    private var cachedRxSpeed: Long = 0L
    private var cachedTxSpeed: Long = 0L

    private var originalTypeface: Typeface? = null

    @SuppressLint("DiscouragedApi")
    override fun onHook() {
        hookController()
        hookView()
    }

    private fun hookController() {
        var networkSpeed = prefs(ModulePrefs).getBoolean("set_network_speed", false)
        dataChannel.wait<Boolean>("set_network_speed") { networkSpeed = it }

        val clazz = VariousClass(
            "com.oplusos.systemui.statusbar.controller.NetworkSpeedController",
            "com.oplus.systemui.statusbar.phone.netspeed.OplusNetworkSpeedControllExImpl",
            "com.oplus.systemui.statusbar.phone.netspeed.OplusNetworkSpeedControllerExImpl"
        ).toClassOrNull(appClassLoader)
        if (clazz == null) {
            YLog.error("StatusBarNetWorkSpeed: NetworkSpeedController not found")
            return
        }

        val bgHandler = clazz.findFieldByName("bgHandler")
        val uiHandler = clazz.findFieldByName("uiHandler")
        val lastTime = clazz.findFieldByName("lastTime")
        val lastTotalBytes = clazz.findFieldByName("lastTotalBytes")
        if (uiHandler == null || lastTime == null || lastTotalBytes == null) {
            YLog.error("StatusBarNetWorkSpeed: NetworkSpeedController fields missing")
            return
        }

        clazz.method { name { it.startsWith("updateNetworkSpeed") } }.ignored().hook {
            before {
                if (!networkSpeed) return@before
                val self = instance ?: args.getOrNull(0) ?: return@before

                val msg = Message.obtain()
                msg.what = MSG_UPDATE
                val connected = self.readBoolean("isConnected")
                val switchOn = self.readBoolean("isSwitchOn")
                val ui = uiHandler.get(self) as? Handler

                if (connected && switchOn) {
                    val now = System.currentTimeMillis()
                    var total = self.callTotalByte()
                    if (total <= 0L) {
                        runCatching { lastTime.set(self, 0L) }
                        runCatching { lastTotalBytes.set(self, 0L) }
                        total = self.callTotalByte()
                    }
                    val prevTime = (runCatching { lastTime.get(self) }.getOrNull() as? Long) ?: 0L
                    var speed = 0L
                    if (prevTime in 0 until now) {
                        val prevBytes =
                            (runCatching { lastTotalBytes.get(self) }.getOrNull() as? Long) ?: 0L
                        if (prevBytes > 0L && total > 0L && total > prevBytes) {
                            speed = ((total - prevBytes) * 1000L) / (now - prevTime)
                        }
                    }
                    msg.arg1 = 1
                    msg.obj = speed
                    ui?.removeMessages(MSG_UPDATE)
                    ui?.sendMessage(msg)
                    runCatching { lastTime.set(self, now) }
                    runCatching { lastTotalBytes.set(self, total) }
                    val bg = bgHandler?.get(self) as? Handler
                    bg?.removeMessages(MSG_TICK)

                    bg?.sendEmptyMessageDelayed(MSG_TICK, 1000L)
                } else {
                    msg.arg1 = 0
                    ui?.removeMessages(MSG_UPDATE)
                    ui?.sendMessage(msg)
                    runCatching { lastTime.set(self, 0L) }
                    runCatching { lastTotalBytes.set(self, 0L) }
                }
                resultNull()
            }
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun hookView() {
        val layoutMode = prefs(ModulePrefs).getString("statusbar_network_layout", "0")
        val userTypeface = prefs(ModulePrefs).getBoolean("statusbar_network_user_typeface", false)

        var useBold = prefs(ModulePrefs).getBoolean("statusbar_network_use_bold_font_style", false)
        dataChannel.wait<Boolean>("statusbar_network_use_bold_font_style") { useBold = it }
        var noSpace = prefs(ModulePrefs).getBoolean("statusbar_network_no_space", false)
        dataChannel.wait<Boolean>("statusbar_network_no_space") { noSpace = it }
        var noSecond = prefs(ModulePrefs).getBoolean("statusbar_network_no_second", false)
        dataChannel.wait<Boolean>("statusbar_network_no_second") { noSecond = it }
        var noUnit = prefs(ModulePrefs).getBoolean("statusbar_network_no_unit", false)
        dataChannel.wait<Boolean>("statusbar_network_no_unit") { noUnit = it }
        var fontSize = prefs(ModulePrefs).getInt("set_network_speed_font_size", 7)
        dataChannel.wait<Int>("set_network_speed_font_size") { fontSize = it }
        var paddingBottom = prefs(ModulePrefs).getInt("set_network_speed_padding_bottom", 0)
        dataChannel.wait<Int>("set_network_speed_padding_bottom") { paddingBottom = it }
        var rowSpacing = prefs(ModulePrefs).getInt("set_network_speed_double_row_spacing", -1)
        dataChannel.wait<Int>("set_network_speed_double_row_spacing") { rowSpacing = it }

        val viewClass = VariousClass(
            "com.oplusos.systemui.statusbar.widget.NetworkSpeedView",
            "com.oplus.systemui.statusbar.phone.netspeed.widget.NetworkSpeedView"
        ).toClassOrNull(appClassLoader)
        if (viewClass == null) {
            YLog.error("StatusBarNetWorkSpeed: NetworkSpeedView not found")
            return
        }

        val iconStateClass = VariousClass(
            "com.oplusos.systemui.ext.BaseNetworkControllerImplExt\$NetworkSpeedIconState",
            "com.oplus.systemui.statusbar.phone.netspeed.NetworkSpeedIconState"
        ).toClassOrNull(appClassLoader)

        val iconStateField = iconStateClass?.let { viewClass.findFieldByType(it) }
        val blockedField = viewClass.findFieldByName("mBlocked")
        val speedField = viewClass.findFieldByName("mSpeed")
        val numberField = viewClass.findFieldByName("mSpeedNumber")
        val unitField = viewClass.findFieldByName("mSpeedUnit")
        val typefaceField = viewClass.findFieldByType(Typeface::class.java)

        fun pickTypeface(): Typeface? = when {
            !userTypeface -> originalTypeface
            useBold -> Typeface.DEFAULT_BOLD
            else -> Typeface.DEFAULT
        }

        viewClass.method { name = "onFinishInflate" }.ignored().hook {
            after {
                val view = instance as? ViewGroup ?: return@after
                val res = view.resources
                if (numberMarginBottom <= 0) numberMarginBottom = runCatching {
                    res.getDimensionPixelSize(
                        res.getIdentifier(
                            "network_speed_number_margin_bottom", "dimen",
                            StatusBarNetWorkSpeed.packageName
                        )
                    )
                }.getOrDefault(0)
                if (unitMarginTop <= 0) unitMarginTop = runCatching {
                    res.getDimensionPixelSize(
                        res.getIdentifier(
                            "network_speed_unit_margin_top", "dimen",
                            StatusBarNetWorkSpeed.packageName
                        )
                    )
                }.getOrDefault(0)
                originalTypeface = when {
                    typefaceField != null ->
                        runCatching { typefaceField.get(instance) as? Typeface }.getOrNull()
                    else -> (numberField?.get(instance) as? TextView)?.typeface
                }
            }
        }

        if (layoutMode == "0") {

            viewClass.method { name = "applyNetworkState" }.ignored().hook {
                after {
                    val tf = pickTypeface()
                    (numberField?.get(instance) as? TextView)?.typeface = tf
                    (unitField?.get(instance) as? TextView)?.typeface = tf
                }
            }
            return
        }

        viewClass.method { name = "applyNetworkState" }.ignored().hook {
            before {
                val view = instance as? ViewGroup ?: return@before
                val state = args.getOrNull(0)
                if (state == null) {
                    view.visibility = View.GONE
                    runCatching { iconStateField?.set(instance, null) }
                    return@before
                }

                val copy = runCatching { XposedHelpers.callMethod(state, "copy") }.getOrNull()
                val visible = runCatching {
                    XposedHelpers.callMethod(state, "getVisible") as? Boolean
                }.getOrNull() ?: false
                runCatching { iconStateField?.set(instance, copy) }

                val blocked =
                    (runCatching { blockedField?.get(instance) }.getOrNull() as? Boolean) ?: false
                val show = visible && !blocked
                if (show != (view.visibility == View.VISIBLE)) {
                    view.visibility = if (show) View.VISIBLE else View.GONE
                }
                if (!show) return@before

                val speed = runCatching {
                    XposedHelpers.callMethod(state, "getSpeedText") as? Long
                }.getOrNull() ?: 0L
                if (speed < 0L || speed > MAX_SPEED) return@before
                runCatching { speedField?.set(instance, speed) }

                view.layoutParams?.width = ViewGroup.LayoutParams.WRAP_CONTENT
                view.setPadding(0, 0, 0, paddingBottom.dp)

                val number = numberField?.get(instance) as? TextView
                val unit = unitField?.get(instance) as? TextView
                val tf = pickTypeface()
                number?.typeface = tf
                unit?.typeface = tf

                when (layoutMode) {
                    "1" -> {
                        unit?.visibility = View.INVISIBLE
                        number?.apply {
                            text = format(speed, noSpace, noUnit, noSecond)
                            setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize.toFloat() * 2f)
                            gravity = Gravity.CENTER_VERTICAL or Gravity.END
                            layoutParams = LayoutParams(layoutParams).apply {
                                height = LayoutParams.MATCH_PARENT
                            }
                        }
                    }

                    "2" -> {
                        unit?.visibility = View.VISIBLE
                        sampleTraffic()
                        number?.apply {
                            text = format(cachedTxSpeed, noSpace, noUnit, noSecond)
                            setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize.toFloat())
                            if (rowSpacing != -1) layoutParams = LayoutParams(layoutParams).apply {
                                bottomMargin = (rowSpacing.dp / 2) + numberMarginBottom
                            }
                        }
                        unit?.apply {
                            text = format(cachedRxSpeed, noSpace, noUnit, noSecond)
                            setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize.toFloat())
                            if (rowSpacing != -1) layoutParams = LayoutParams(layoutParams).apply {
                                topMargin = (rowSpacing.dp / 2) + unitMarginTop
                            }
                        }
                    }
                }
                resultNull()
            }
        }
    }

    private fun format(bytes: Long, noSpace: Boolean, noUnit: Boolean, noSecond: Boolean): String {
        val value = bytes.toFloat()
        val scaled: Float
        val unit: String
        when {
            value >= 1048576f -> {
                scaled = value / 1048576f; unit = "MB"
            }

            value >= 1024f -> {
                scaled = value / 1024f; unit = "KB"
            }

            else -> {
                scaled = value; unit = "B"
            }
        }
        val head = runCatching { String.format(Locale.US, "%.1f", scaled) }.getOrDefault("0.0")
        val gap = if (noSpace) "" else " "
        val tail = if (noUnit) "" else unit + (if (noSecond) "" else "/s")
        return head + gap + tail
    }

    private fun sampleTraffic() {
        val nowNanos = SystemClock.elapsedRealtimeNanos()
        val rx = if (SDK >= 31) {
            TrafficStats.getTotalRxBytes() - TrafficStats.getRxBytes("lo")
        } else TrafficStats.getTotalRxBytes()
        val tx = if (SDK >= 31) {
            TrafficStats.getTotalTxBytes() - TrafficStats.getTxBytes("lo")
        } else TrafficStats.getTotalTxBytes()

        if (lastSampleNanos == 0L) {
            lastSampleNanos = nowNanos
            lastRxBytes = rx
            lastTxBytes = tx
            cachedRxSpeed = 0L
            cachedTxSpeed = 0L
            return
        }
        val deltaNanos = nowNanos - lastSampleNanos
        if (deltaNanos < 20_000_000L) return
        val seconds = deltaNanos / 1.0E9
        if (seconds > 0.1) {
            cachedRxSpeed = (((rx - lastRxBytes) / seconds).toLong()).coerceAtLeast(0L)
            cachedTxSpeed = (((tx - lastTxBytes) / seconds).toLong()).coerceAtLeast(0L)
        }
        lastSampleNanos = nowNanos
        lastRxBytes = rx
        lastTxBytes = tx
    }

    private fun Any.readBoolean(field: String): Boolean =
        (runCatching { XposedHelpers.getObjectField(this, field) }.getOrNull() as? Boolean) ?: false

    private fun Any.callTotalByte(): Long =
        (runCatching { XposedHelpers.callMethod(this, "getTotalByte") }.getOrNull() as? Long) ?: 0L

    private fun Class<*>.findFieldByName(name: String): Field? {
        var c: Class<*>? = this
        while (c != null && c != Any::class.java) {
            c.declaredFields.firstOrNull { it.name == name }
                ?.let { return it.apply { isAccessible = true } }
            c = c.superclass
        }
        return null
    }

    private fun Class<*>.findFieldByType(type: Class<*>): Field? {
        var c: Class<*>? = this
        while (c != null && c != Any::class.java) {
            c.declaredFields.firstOrNull { it.type == type }
                ?.let { return it.apply { isAccessible = true } }
            c = c.superclass
        }
        return null
    }

    private const val MSG_UPDATE = 100000
    private const val MSG_TICK = 100001

    private const val MAX_SPEED = 1_125_899_906_842_624_000L
}
