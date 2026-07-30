package com.fosstool.app.hook.scope.systemui

import android.view.View
import androidx.core.view.isVisible
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import java.lang.reflect.Field

object RemoveRotateScreenButton : YukiBaseHooker() {
    override fun onHook() {
        VariousClass(
            "com.android.systemui.statusbar.phone.FloatingRotationButton",
            "com.android.systemui.navigationbar.gestural.FloatingRotationButton",
            "com.android.systemui.shared.rotation.FloatingRotationButton"
        ).toClassOrNull(appClassLoader)?.let { c ->
            c.declaredConstructors.forEach { ctor ->
                if (ctor.parameterTypes.isEmpty() ||
                    !android.content.Context::class.java.isAssignableFrom(ctor.parameterTypes[0])
                ) return@forEach
                runCatching {
                    XposedBridge.hookMethod(ctor, object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            (c.findField("mKeyButtonView")?.get(param.thisObject) as? View)?.isVisible =
                                false
                        }
                    })
                }
            }
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
