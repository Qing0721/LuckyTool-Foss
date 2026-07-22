package com.fosstool.app.hook.scope.systemui

import android.content.res.ColorStateList
import android.view.View
import android.widget.CheckBox
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK
import com.fosstool.app.utils.safeOfNan

object ControlCenterSliderTransparency : YukiBaseHooker() {
    override fun onHook() {
        var alpha = prefs(ModulePrefs).getInt("custom_control_center_silder_transparency", -1)
        dataChannel.wait<Int>("custom_control_center_silder_transparency") { alpha = it }

        VariousClass(
            "com.oplusos.systemui.qs.widget.OplusToggleSliderView",
            "com.oplus.systemui.qs.widget.OplusToggleSliderView"
        ).toClass().apply {
            method { name = "setupSliderProgressDrawable" }.hook {
                after {
                    if (alpha < 0) return@after
                    val slider = field { name = "mSlider" }.get(instance).any() ?: return@after
                    val color = field { name = "mProgressColor" }.get(slider).cast<Int>() ?: return@after
                    val ratio = alpha / 10.0F
                    val newColor = color.colorAlphaOf(ratio)
                    slider.current().method { name = "setProgressColor" }.call(ColorStateList.valueOf(newColor))
                    slider.current().method { name = "setThumbColor" }.call(ColorStateList.valueOf(0))
                }
            }
            method { name = "updateToggleBackground" }.hook {
                after {
                    if (alpha < 0) return@after
                    val toggle = field { name = "mToggle" }.get(instance).cast<CheckBox>() ?: return@after
                    toggle.background?.setAlpha((alpha * 25).coerceIn(0, 255))
                }
            }
        }

        if (SDK >= A13) {
            "com.oplus.systemui.qs.widget.OplusQsToggleSliderLayout".toClass().apply {
                method { name = "generateSliderView" }.hook {
                    after {
                    if (alpha < 0) return@after
                    val view = result<View>() ?: return@after
                    val color = field { name = "mProgressColor" }.get(view).cast<Int>() ?: return@after
                    val newColor = color.colorAlphaOf(alpha / 10.0F)
                    view.current().method { name = "setProgressColor" }.call(ColorStateList.valueOf(newColor))
                    }
                }
            }
        }
    }

    private fun Int.colorAlphaOf(value: Float) =
        safeOfNan { (255.coerceAtMost(0.coerceAtLeast((value * 255).toInt())) shl 24) + (0x00ffffff and this) }
}
