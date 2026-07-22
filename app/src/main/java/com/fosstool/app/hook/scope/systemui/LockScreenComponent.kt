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
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.A14
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK
import com.fosstool.app.utils.dp

object LockScreenComponent : YukiBaseHooker() {
    override fun onHook() {
        val isCenter = prefs(ModulePrefs).getBoolean("set_lock_screen_centered", false)
        val userTypeface =
            prefs(ModulePrefs).getBoolean("lock_screen_clock_use_user_typeface", false)

        VariousClass(
            "com.oplusos.systemui.keyguard.clock.RedHorizontalSingleClockView",
            "com.oplus.systemui.shared.clocks.RedHorizontalSingleClockView"
        ).toClass().apply {
            method { name = "onFinishInflate" }.hook {
                after {
                    if (!isCenter) return@after
                    instance<LinearLayout>().setPadding(0, 20.dp, 0, 0)

                    field { name = "mTvWeek" }.get(instance).cast<TextView>()
                        ?.setCenterHorizontally()


                    (field { name = "mTvColon" }.get(instance)
                        .cast<TextView>()?.parent as RelativeLayout).setCenterHorizontally()

                    field { name = "mTvDate" }.get(instance).cast<TextView>()
                        ?.setCenterHorizontally()

                    field { name = "mTvLunarCalendar" }.get(instance).cast<TextView>()
                        ?.setCenterHorizontally()

                    field { name = "mTvExtraContent" }.get(instance).cast<TextView>()
                        ?.setCenterHorizontally()
                }
            }
            method { name = "setTextFont" }.hook {
                if (userTypeface) intercept()
            }
        }

        VariousClass(
            "com.oplusos.systemui.keyguard.clock.SingleClockView",
            "com.oplus.systemui.shared.clocks.SingleClockView"
        ).toClass().apply {
            method { name = "onFinishInflate" }.hook {
                after {
                    if (!isCenter && !userTypeface) return@after
                    val vg = instance<ViewGroup>()
                    if (isCenter) {
                        vg.setPadding(0, 20.dp, 0, 0)
                        for (i in 0 until vg.childCount) {
                            vg.getChildAt(i)?.setCenterHorizontally()
                        }
                    }
                    if (userTypeface) vg.resetDescendantTypefacesToDefault()
                }
            }
            method { name = "updateKeyguardLandClock" }.hook {
                after {
                    if (!isCenter) return@after
                    instance<ViewGroup>().setPadding(0, 20.dp, 0, 0)
                }
            }
        }

        VariousClass(
            "com.oplusos.systemui.keyguard.clock.DualClockView",
            "com.oplus.systemui.shared.clocks.DualClockView"
        ).toClass().apply {
            method { name = "onFinishInflate" }.hook {
                after {
                    if (!userTypeface) return@after
                    instance<ViewGroup>().resetDescendantTypefacesToDefault()
                }
            }
        }

        VariousClass(
            "com.oplusos.systemui.keyguard.clock.RedHorizontalDualClockView",
            "com.oplus.systemui.shared.clocks.RedHorizontalDualClockView"
        ).toClass().apply {
            method { name = "onFinishInflate" }.hook {
                after {
                    if (!userTypeface) return@after
                    instance<ViewGroup>().resetDescendantTypefacesToDefault()
                }
            }
            method { name = "setTextFont" }.hook {
                if (userTypeface) intercept()
            }
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
}
