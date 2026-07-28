package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.utils.getOSVersionCode
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import java.lang.reflect.Field

object VibrateWhenOpeningTheStatusBar : YukiBaseHooker() {
    override fun onHook() {

        if (getOSVersionCode < 26) return

        VariousClass(
            "com.android.systemui.statusbar.phone.PanelViewController",
            "com.android.systemui.shade.NotificationPanelViewController"
        ).toClassOrNull(appClassLoader)?.let { c ->
            c.declaredConstructors.forEach { ctor ->
                runCatching {
                    XposedBridge.hookMethod(ctor, object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            c.findField("mVibrateOnOpening")?.set(param.thisObject, true)
                        }
                    })
                }
            }
        }

        VariousClass(
            "com.android.systemui.statusbar.phone.StatusBarCommandQueueCallbacks",
            "com.android.systemui.statusbar.phone.CentralSurfacesCommandQueueCallbacks"
        ).toClassOrNull(appClassLoader)?.let { c ->
            if (c.findField("mVibrateOnOpening") == null) return@let
            c.declaredConstructors.forEach { ctor ->
                runCatching {
                    XposedBridge.hookMethod(ctor, object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            c.findField("mVibrateOnOpening")?.set(param.thisObject, true)
                        }
                    })
                }
            }
        }

        "com.android.systemui.statusbar.phone.StatusBar"
            .toClassOrNull(appClassLoader)?.let { c ->
                if (c.findField("mVibrateOnOpening") == null) return@let
                c.method { name = "start" }.ignored().hook {
                    after {
                        c.findField("mVibrateOnOpening")?.set(instance, true)
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
