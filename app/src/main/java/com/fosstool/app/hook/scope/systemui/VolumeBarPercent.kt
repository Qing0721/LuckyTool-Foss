package com.fosstool.app.hook.scope.systemui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.view.View
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClass
import com.highcapable.yukihookapi.hook.type.android.CanvasClass
import com.fosstool.app.utils.ModulePrefs

object VolumeBarPercent : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("enable_volume_bar_percent_display", false)) return

        var color = prefs(ModulePrefs).getString("custom_volume_bar_percent_color", "#FFFFFFFF")
        dataChannel.wait<String>("custom_volume_bar_percent_color") { color = it }

        "com.oplus.systemui.volume.OplusVolumeSeekBar".toClass().apply {
            method {
                name = "drawActiveTrack"
                param(CanvasClass)
            }.hook {
                before {
                    val canvas = args().first().cast<Canvas>() ?: return@before
                    val view = instance<View>() ?: return@before

                    val progress = try {
                        view.current().method { name = "getProgress" }.invoke<Int>()
                            ?: return@before
                    } catch (e: Throwable) {
                        return@before
                    }
                    val max = try {
                        view.current().method { name = "getMax" }.invoke<Int>()
                            ?: return@before
                    } catch (e: Throwable) {
                        return@before
                    }
                    if (max <= 0) return@before

                    val width = view.width
                    val height = view.height
                    if (width <= 0 || height <= 0) return@before

                    val percentage = progress * 100 / max

                    val density = try {
                        view.resources.displayMetrics.density
                    } catch (e: Throwable) {
                        1f
                    }

                    val paint = TextPaint().apply {
                        isAntiAlias = true
                        try {
                            this.color = Color.parseColor(color)
                        } catch (e: Throwable) {
                            this.color = Color.WHITE
                        }
                        textSize = density * 12f
                        textAlign = Paint.Align.CENTER
                        typeface = Typeface.DEFAULT_BOLD
                    }

                    val x = width / 2.0f
                    var y = height * 0.25f - density * 10f
                    if (y < paint.textSize) y = paint.textSize

                    canvas.drawText("${percentage}%", x, y, paint)
                }
            }
        }
    }
}
