package com.fosstool.app.hook.scope.systemui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.widget.ProgressBar
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.utils.ModulePrefs

object ControlCenterProgressPercent : YukiBaseHooker() {

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 28f
        color = Color.WHITE
    }

    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("enable_control_center_progress_percent_display", false)) {
            return
        }
        val colorStr = prefs(ModulePrefs).getString(
            "custom_control_center_progress_percent_color",
            "#FFFFFFFF"
        ) ?: "#FFFFFFFF"
        runCatching {
            textPaint.color = Color.parseColor(colorStr)
        }

        "com.oplus.systemui.qs.base.seek.OplusQsVerticalSeekBar"
            .toClassOrNull(appClassLoader)
            ?.method { name = "onDraw"; paramCount = 1 }?.ignored()?.hook {
                after {
                    val canvas = args.getOrNull(0) as? Canvas ?: return@after
                    val bar = instance as? ProgressBar ?: return@after
                    val max = bar.max.coerceAtLeast(1)
                    val progress = bar.progress
                    val percent = (progress * 100 / max).coerceIn(0, 100)
                    val text = "$percent%"
                    val cx = bar.width / 2f
                    val cy = (bar.height * 0.55f).coerceAtLeast(textPaint.textSize)
                    canvas.drawText(text, cx, cy, textPaint)
                }
            }
    }
}
