package com.fosstool.app.hook.scope.systemui

import android.content.res.ColorStateList
import android.view.View
import android.widget.CheckBox
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK
import com.fosstool.app.utils.safeOfNan
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Field

object ControlCenterSliderTransparency : YukiBaseHooker() {
    override fun onHook() {
        var alpha = prefs(ModulePrefs).getInt("custom_control_center_silder_transparency", -1)
        dataChannel.wait<Int>("custom_control_center_silder_transparency") { alpha = it }

        VariousClass(
            "com.oplusos.systemui.qs.widget.OplusToggleSliderView",
            "com.oplus.systemui.qs.widget.OplusToggleSliderView"
        ).toClassOrNull(appClassLoader)?.let { c ->
            c.method { name = "setupSliderProgressDrawable" }.ignored().hook {
                after {
                    if (alpha < 0) return@after
                    val slider = c.findField("mSlider")?.get(instance) ?: return@after
                    val color = slider.javaClass.findField("mProgressColor")?.get(slider) as? Int
                        ?: return@after
                    val ratio = alpha / 10.0F
                    val newColor = color.colorAlphaOf(ratio)
                    runCatching {
                        XposedHelpers.callMethod(
                            slider,
                            "setProgressColor",
                            ColorStateList.valueOf(newColor)
                        )
                        XposedHelpers.callMethod(slider, "setThumbColor", ColorStateList.valueOf(0))
                    }
                }
            }
            c.method { name = "updateToggleBackground" }.ignored().hook {
                after {
                    if (alpha < 0) return@after
                    val toggle = c.findField("mToggle")?.get(instance) as? CheckBox
                        ?: return@after
                    toggle.background?.setAlpha((alpha * 25).coerceIn(0, 255))
                }
            }
        }

        "com.oplus.systemui.qs.widget.OplusQsToggleSliderLayout"
            .toClassOrNull(appClassLoader)?.let { c ->
                c.method {
                    name = "generateSliderView"
                    if (SDK >= 35) superClass()
                }.ignored().hook {
                    after {
                        if (alpha < 0) return@after
                        val view = result as? View ?: return@after
                        val color = view.javaClass.findField("mProgressColor")?.get(view) as? Int
                            ?: return@after
                        val newColor = color.colorAlphaOf(alpha / 10.0F)
                        runCatching {
                            XposedHelpers.callMethod(
                                view,
                                "setProgressColor",
                                ColorStateList.valueOf(newColor)
                            )
                        }
                    }
                }
            }
    }

    private fun Int.colorAlphaOf(value: Float) =
        safeOfNan { (255.coerceAtMost(0.coerceAtLeast((value * 255).toInt())) shl 24) + (0x00ffffff and this) }

    private fun Class<*>.findField(name: String): Field? {
        var cls: Class<*>? = this
        while (cls != null) {
            runCatching { return cls.getDeclaredField(name).also { it.isAccessible = true } }
            cls = cls.superclass
        }
        return null
    }
}
