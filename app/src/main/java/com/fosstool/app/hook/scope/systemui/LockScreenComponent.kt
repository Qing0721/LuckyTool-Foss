package com.fosstool.app.hook.scope.systemui

import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.dp
import java.lang.reflect.Field

object LockScreenComponent : YukiBaseHooker() {
    override fun onHook() {
        val isCenter = prefs(ModulePrefs).getBoolean("set_lock_screen_centered", false)
        val userTypeface =
            prefs(ModulePrefs).getBoolean("lock_screen_clock_use_user_typeface", false)

        VariousClass(
            "com.oplusos.systemui.keyguard.clock.RedHorizontalSingleClockView",
            "com.oplus.systemui.shared.clocks.RedHorizontalSingleClockView"
        ).toClassOrNull(appClassLoader)?.let { c ->
            c.method { name = "onFinishInflate" }.ignored().hook {
                after {
                    if (!isCenter) return@after
                    (instance as? LinearLayout)?.setPadding(0, 20.dp, 0, 0)

                    (c.findField("mTvWeek")?.get(instance) as? TextView)
                        ?.setCenterHorizontally()

                    ((c.findField("mTvColon")?.get(instance) as? TextView)?.parent as? RelativeLayout)
                        ?.setCenterHorizontally()

                    (c.findField("mTvDate")?.get(instance) as? TextView)
                        ?.setCenterHorizontally()

                    (c.findField("mTvLunarCalendar")?.get(instance) as? TextView)
                        ?.setCenterHorizontally()

                    (c.findField("mTvExtraContent")?.get(instance) as? TextView)
                        ?.setCenterHorizontally()
                }
            }
            if (userTypeface) c.method { name = "setTextFont" }.ignored().hook { intercept() }
        }

        VariousClass(
            "com.oplusos.systemui.keyguard.clock.SingleClockView",
            "com.oplus.systemui.shared.clocks.SingleClockView"
        ).toClassOrNull(appClassLoader)?.let { c ->
            c.method { name = "onFinishInflate" }.ignored().hook {
                after {
                    if (!isCenter && !userTypeface) return@after
                    val vg = instance as? ViewGroup ?: return@after
                    if (isCenter) {
                        vg.setPadding(0, 20.dp, 0, 0)
                        for (i in 0 until vg.childCount) {
                            vg.getChildAt(i)?.setCenterHorizontally()
                        }
                    }
                    if (userTypeface) vg.resetDescendantTypefacesToDefault()
                }
            }
            c.method { name = "updateKeyguardLandClock" }.ignored().hook {
                after {
                    if (!isCenter) return@after
                    (instance as? ViewGroup)?.setPadding(0, 20.dp, 0, 0)
                }
            }
        }

        VariousClass(
            "com.oplusos.systemui.keyguard.clock.DualClockView",
            "com.oplus.systemui.shared.clocks.DualClockView"
        ).toClassOrNull(appClassLoader)
            ?.method { name = "onFinishInflate" }?.ignored()?.hook {
                after {
                    if (!userTypeface) return@after
                    (instance as? ViewGroup)?.resetDescendantTypefacesToDefault()
                }
            }

        VariousClass(
            "com.oplusos.systemui.keyguard.clock.RedHorizontalDualClockView",
            "com.oplus.systemui.shared.clocks.RedHorizontalDualClockView"
        ).toClassOrNull(appClassLoader)?.let { c ->
            c.method { name = "onFinishInflate" }.ignored().hook {
                after {
                    if (!userTypeface) return@after
                    (instance as? ViewGroup)?.resetDescendantTypefacesToDefault()
                }
            }
            if (userTypeface) c.method { name = "setTextFont" }.ignored().hook { intercept() }
        }
    }

    private fun View.setCenterHorizontally() {
        layoutParams = LinearLayout.LayoutParams(layoutParams).apply {
            gravity = Gravity.CENTER_HORIZONTAL
        }
    }

    private fun ViewGroup.resetDescendantTypefacesToDefault() {
        for (i in 0 until childCount) {
            val child = getChildAt(i) ?: continue
            if (child is TextView) child.typeface = Typeface.DEFAULT
            if (child is ViewGroup) child.resetDescendantTypefacesToDefault()
        }
    }

    private fun Class<*>.findField(name: String): Field? {
        var cls: Class<*>? = this
        while (cls != null) {
            runCatching { return cls.getDeclaredField(name).also { it.isAccessible = true } }
            cls = cls.superclass
        }
        return null
    }
}
