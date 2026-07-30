package com.fosstool.app.hook.scope.systemui

import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.constructor
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.utils.ModulePrefs
import java.lang.reflect.Field

object FullScreenGestureSideSlideBar : YukiBaseHooker() {
    override fun onHook() {
        val removeView = prefs(ModulePrefs).getBoolean("remove_side_slider", false)
        val removeBackground =
            prefs(ModulePrefs).getBoolean("remove_side_slider_black_background", false)
        val isReplace = prefs(ModulePrefs).getBoolean("replace_side_slider_icon_switch", false)

        val leftPath =
            prefs(ModulePrefs).getString("replace_side_slider_icon_on_left", "")
        val rightPath =
            prefs(ModulePrefs).getString("replace_side_slider_icon_on_right", "")
        VariousClass(
            "com.oplusos.systemui.navbar.gesture.sidegesture.SideGestureNavView",
            "com.oplusos.systemui.navigationbar.gesture.sidegesture.SideGestureNavView",
            "com.oplus.systemui.navigationbar.gesture.sidegesture.SideGestureNavView",
            "com.oplus.systemui.navigationbar.gesture.sidegesture.view.SideGestureNavView"
        ).toClassOrNull(appClassLoader)?.let { c ->
            if (removeView) c.method { name = "onDraw"; paramCount = 1 }.ignored().hook { intercept() }

            val hasInitPaint = c.hasMethod("initPaint")
            if (hasInitPaint) {
                c.method { name = "initPaint"; superClass() }.ignored().hook {
                    after { if (removeBackground) clearBezierPaint(c, instance) }
                }
            } else {
                c.constructor().ignored().hookAll {
                    after { if (removeBackground) clearBezierPaint(c, instance) }
                }
            }
            c.method { name = "setBackIcon"; paramCount = 1 }.ignored().hook {
                before {
                    if (!isReplace) return@before
                    val pos = c.findField("mPosition")?.get(instance) as? Int
                        ?: return@before
                    val res = when (pos) {
                        0 -> BitmapFactory.decodeFile(leftPath)
                        1 -> BitmapFactory.decodeFile(rightPath)
                        else -> return@before
                    }
                    res?.let { args[0] = it }
                }
            }
        }
    }

    private fun clearBezierPaint(c: Class<*>, instance: Any?) {
        val paint = (c.findField("mBezierPaint")?.get(instance) as? Paint)
            ?: (c.findBezierPaintField()?.get(instance) as? Paint)
            ?: return
        paint.color = Color.TRANSPARENT
    }

    private fun Class<*>.findBezierPaintField(): Field? {
        var cls: Class<*>? = this
        while (cls != null) {
            cls.declaredFields.firstOrNull {
                it.type == Paint::class.java && it.name.contains("bezierPaint", ignoreCase = true)
            }?.let { f -> return f.also { it.isAccessible = true } }
            cls = cls.superclass
        }
        return null
    }

    private fun Class<*>.hasMethod(name: String): Boolean {
        var cls: Class<*>? = this
        while (cls != null) {
            if (cls.declaredMethods.any { it.name == name }) return true
            cls = cls.superclass
        }
        return false
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
