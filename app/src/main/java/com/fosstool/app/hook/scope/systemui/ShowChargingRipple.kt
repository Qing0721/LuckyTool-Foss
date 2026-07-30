package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.utils.A14
import com.fosstool.app.utils.SDK
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import java.lang.reflect.Field

object ShowChargingRipple : YukiBaseHooker() {
    override fun onHook() {
        VariousClass(
            "com.android.systemui.statusbar.charging.WiredChargingRippleController",
            "com.android.systemui.charging.WiredChargingRippleController"
        ).toClassOrNull(appClassLoader)?.let { c ->
            c.declaredConstructors.forEach { ctor ->
                runCatching {
                    XposedBridge.hookMethod(ctor, object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            c.findField("rippleEnabled")?.set(param.thisObject, true)
                        }
                    })
                }
            }
        }
        if (SDK >= A14) return
        "com.android.systemui.statusbar.FeatureFlags"
            .toClassOrNull(appClassLoader)
            ?.method { name = "isChargingRippleEnabled" }?.ignored()?.hook { replaceToTrue() }
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
