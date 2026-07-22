package com.fosstool.app.hook.statusbar

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.net.TrafficStats
import android.util.TypedValue
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams
import android.widget.TextView
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.dp
import java.text.DecimalFormat

object StatusBarNetWorkSpeed : YukiBaseHooker() {

    private var mLastTotalUp: Long = 0L

    private var mLastTotalDown: Long = 0L

    private var lastTimeStampTotalUp: Long = 0L

    private var lastTimeStampTotalDown: Long = 0L

    @SuppressLint("DiscouragedApi")
    override fun onHook() {
        var networkSpeed = prefs(ModulePrefs).getBoolean("set_network_speed", false)
        dataChannel.wait<Boolean>("set_network_speed") { networkSpeed = it }

        VariousClass(
            "com.oplusos.systemui.statusbar.controller.NetworkSpeedController",
            "com.oplus.systemui.statusbar.phone.netspeed.OplusNetworkSpeedControllExImpl",
            "com.oplus.systemui.statusbar.phone.netspeed.OplusNetworkSpeedControllerExImpl"
        ).toClass().apply {
            method {
                name = "postUpdateNetworkSpeedDelay"
                paramCount = 1
            }.hook {
                before {
                    if (networkSpeed && (args().first().long() == 4000L)) {
                        args(0).set(1000L)
                    }
                }
            }
        }

        val layoutMode = prefs(ModulePrefs).getString("statusbar_network_layout", "0")
        val userTypeface =
            prefs(ModulePrefs).getBoolean("statusbar_network_user_typeface", false)
        var noSpace = prefs(ModulePrefs).getBoolean("statusbar_network_no_space", false)
        dataChannel.wait<Boolean>("statusbar_network_no_space") { noSpace = it }
        var noSecond = prefs(ModulePrefs).getBoolean("statusbar_network_no_second", false)
        dataChannel.wait<Boolean>("statusbar_network_no_second") { noSecond = it }
        var noUnit = prefs(ModulePrefs).getBoolean("statusbar_network_no_unit", false)
        dataChannel.wait<Boolean>("statusbar_network_no_unit") { noUnit = it }
        var getDoubleSize = prefs(ModulePrefs).getInt("set_network_speed_font_size", 7)
        dataChannel.wait<Int>("set_network_speed_font_size") { getDoubleSize = it }
        var getBottomPadding = prefs(ModulePrefs).getInt("set_network_speed_padding_bottom", 0)
        dataChannel.wait<Int>("set_network_speed_padding_bottom") { getBottomPadding = it }
        var setInterval = prefs(ModulePrefs).getInt("set_network_speed_double_row_spacing", -1)
        dataChannel.wait<Int>("set_network_speed_double_row_spacing") { setInterval = it }

        var bMargin = 0
        var tMargin = 0

        VariousClass(
            "com.oplusos.systemui.statusbar.widget.NetworkSpeedView",
            "com.oplus.systemui.statusbar.phone.netspeed.widget.NetworkSpeedView"
        ).toClass().apply {
            method { name = "onFinishInflate" }.hook {
                after {
                    val mView = instance<FrameLayout>()
                    val mSpeedNumber =
                        field { name = "mSpeedNumber" }.get(instance).cast<TextView>()
                    val mSpeedUnit = field { name = "mSpeedUnit" }.get(instance).cast<TextView>()
                    if (userTypeface) {
                        mSpeedNumber?.typeface = Typeface.DEFAULT_BOLD
                        mSpeedUnit?.typeface = Typeface.DEFAULT_BOLD
                    }
                    when (layoutMode) {
                        "1" -> {
                            val speedUnit: TextView? =
                                mView.resources.getIdentifier(
                                    "unit", "id",
                                    StatusBarNetWorkSpeed.packageName
                                )
                                    .let { mView.findViewById(it) }
                            mView.removeView(speedUnit)
                        }
                    }
                    if (bMargin <= 0) bMargin = instance<FrameLayout>().resources.let {
                        it.getDimensionPixelSize(
                            it.getIdentifier(
                                "network_speed_number_margin_bottom",
                                "dimen", StatusBarNetWorkSpeed.packageName
                            )
                        )
                    }
                    if (tMargin <= 0) tMargin = instance<FrameLayout>().resources.let {
                        it.getDimensionPixelSize(
                            it.getIdentifier(
                                "network_speed_unit_margin_top",
                                "dimen", StatusBarNetWorkSpeed.packageName
                            )
                        )
                    }
                }
            }
            method { name = "updateNetworkSpeed" }.hook {
                before {
                    if (layoutMode == "0") return@before
                    instance<FrameLayout>().apply {
                        layoutParams?.width = LayoutParams.WRAP_CONTENT
                        setPadding(0, 0, 0, getBottomPadding.dp)
                    }
                    val mSpeedNumber =
                        field { name = "mSpeedNumber" }.get(instance).cast<TextView>()
                    val mSpeedUnit = field { name = "mSpeedUnit" }.get(instance).cast<TextView>()
                    when (layoutMode) {
                        "1" -> {
                            var speed = args().first().string()
                            if (noUnit) speed = speed.replace(Regex("[KMGTP]?B/s"), "/s")
                            if (noSecond) speed = speed.replace("/s", "")
                            if (noSpace) speed = speed.replace(" ", "")
                            mSpeedNumber?.apply {
                                text = speed
                                setTextSize(
                                    TypedValue.COMPLEX_UNIT_DIP,
                                    getDoubleSize.toFloat() * 2
                                )
                                gravity = Gravity.CENTER_VERTICAL or Gravity.END
                                layoutParams = LayoutParams(layoutParams).apply {
                                    height = LayoutParams.MATCH_PARENT
                                }
                            }
                        }

                        "2" -> {
                            mSpeedNumber?.apply {
                                text = getTotalUpSpeed().let {
                                    if (noUnit) it.replace(Regex("[KMGTP]?B/s"), "/s") else it
                                }.let {
                                    if (noSecond) it.replace(
                                        "/s", ""
                                    ) else it
                                }
                                setTextSize(
                                    TypedValue.COMPLEX_UNIT_DIP,
                                    getDoubleSize.toFloat()
                                )
                                if (setInterval != -1) layoutParams =
                                    LayoutParams(layoutParams).apply {
                                        bottomMargin = bMargin + (setInterval.dp / 2)
                                    }
                            }
                            mSpeedUnit?.apply {
                                text = getTotalDownloadSpeed().let {
                                    if (noUnit) it.replace(Regex("[KMGTP]?B/s"), "/s") else it
                                }.let {
                                    if (noSecond) it.replace(
                                        "/s", ""
                                    ) else it
                                }
                                setTextSize(
                                    TypedValue.COMPLEX_UNIT_DIP,
                                    getDoubleSize.toFloat()
                                )
                                if (setInterval != -1) layoutParams =
                                    LayoutParams(layoutParams).apply {
                                        topMargin = tMargin + (setInterval.dp / 2)
                                    }
                            }
                        }
                    }
                    instance<FrameLayout>().requestLayout()
                    resultNull()
                }
            }
        }
    }

    private fun getTotalUpSpeed(): String {
        val totalUpSpeed: Float
        val currentTotalTxBytes = TrafficStats.getTotalTxBytes()

        val nowTimeStampTotalUp = System.currentTimeMillis()

        val mCurrentTotalUp = currentTotalTxBytes - mLastTotalUp

        val mCurrentIntervals = nowTimeStampTotalUp - lastTimeStampTotalUp

        val bytes = ((mCurrentTotalUp * 1000.0) / (mCurrentIntervals * 1.0)).toFloat()
        if (bytes.isInfinite() || bytes.isNaN()) return "0B/s"
        val unit: String
        if (bytes >= (1024 * 1024)) {
            totalUpSpeed = DecimalFormat("0.0").format(bytes / (1024 * 1024)).toFloat()
            unit = "MB/s"
        } else if (bytes >= 1024) {
            totalUpSpeed = DecimalFormat("0.0").format(bytes / 1024).toFloat()
            unit = "KB/s"
        } else {
            totalUpSpeed = DecimalFormat("0.0").format(bytes).toFloat()
            unit = "B/s"
        }
        mLastTotalUp = currentTotalTxBytes
        lastTimeStampTotalUp = nowTimeStampTotalUp
        return totalUpSpeed.toString() + unit
    }

    private fun getTotalDownloadSpeed(): String {
        val totalDownSpeed: Float
        val currentTotalRxBytes = TrafficStats.getTotalRxBytes()

        val nowTimeStampTotalDown = System.currentTimeMillis()

        val mCurrentTotalDown = currentTotalRxBytes - mLastTotalDown

        val mCurrentIntervals = nowTimeStampTotalDown - lastTimeStampTotalDown

        val bytes = ((mCurrentTotalDown * 1000.0) / (mCurrentIntervals * 1.0)).toFloat()
        if (bytes.isInfinite() || bytes.isNaN()) return "0B/s"
        val unit: String
        if (bytes >= (1024 * 1024)) {
            totalDownSpeed = DecimalFormat("0.0").format(bytes / (1024 * 1024)).toFloat()
            unit = "MB/s"
        } else if (bytes >= 1024) {
            totalDownSpeed = DecimalFormat("0.0").format(bytes / 1024).toFloat()
            unit = "KB/s"
        } else {
            totalDownSpeed = DecimalFormat("0.0").format(bytes).toFloat()
            unit = "B/s"
        }
        mLastTotalDown = currentTotalRxBytes
        lastTimeStampTotalDown = nowTimeStampTotalDown

        return totalDownSpeed.toString() + unit
    }
}
