package com.fosstool.app.hook.scope.systemui

import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.BitmapClass
import com.highcapable.yukihookapi.hook.type.android.PaintClass
import com.fosstool.app.utils.ModulePrefs

object FullScreenGestureSideSlideBar : YukiBaseHooker() {
    override fun onHook() {
        val removeView = prefs(ModulePrefs).getBoolean("remove_side_slider", false)
        val removeBackground =
            prefs(ModulePrefs).getBoolean("remove_side_slider_black_background", false)
        val isReplace = prefs(ModulePrefs).getBoolean("replace_side_slider_icon_switch", false)
        val leftPath =
            prefs(ModulePrefs).getString("replace_side_slider_icon_on_left", "null")
        val rightPath =
            prefs(ModulePrefs).getString("replace_side_slider_icon_on_right", "null")
        VariousClass(
            "com.oplusos.systemui.navbar.gesture.sidegesture.SideGestureNavView",
            "com.oplusos.systemui.navigationbar.gesture.sidegesture.SideGestureNavView",
            "com.oplus.systemui.navigationbar.gesture.sidegesture.SideGestureNavView",
            "com.oplus.systemui.navigationbar.gesture.sidegesture.view.SideGestureNavView"
        ).toClass().apply {
            method { name = "onDraw";paramCount = 1 }.hook {
                if (removeView) intercept()
            }
            method { name = "initPaint";emptyParam() }.hook {
                after {
                    if (!removeBackground) return@after
                    field { name = "mBezierPaint";type = PaintClass }.get(instance)
                        .cast<Paint>()?.color = Color.TRANSPARENT
                }
            }
            method { name = "setBackIcon";param(BitmapClass) }.hook {
                before {
                    if (!isReplace) return@before
                    val res = when (field { name = "mPosition" }.get(instance).int()) {
                        0 -> BitmapFactory.decodeFile(leftPath)
                        1 -> BitmapFactory.decodeFile(rightPath)
                        else -> return@before
                    }
                    res?.let { args(0).set(it) }
                }
            }
        }
    }
}
