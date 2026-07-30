package com.fosstool.app.hook.scope.systemui

import android.view.View
import androidx.core.view.isVisible
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import java.lang.reflect.Field

object RemoveLockScreenBottomSOSButton : YukiBaseHooker() {
    override fun onHook() {
        VariousClass(
            "com.oplus.systemui.keyguard.OplusEmergencyButtonControllExImpl",
            "com.oplus.keyguard.OplusEmergencyButtonExImpl"
        ).toClassOrNull(appClassLoader)?.let { c ->
            val disableShow = runCatching {
                c.getDeclaredMethod("disableShowEmergencyButton")
            }.getOrNull()
            if (disableShow != null) {
                c.method { name = "disableShowEmergencyButton" }.ignored().hook { replaceToTrue() }
            } else {
                c.method { name = "shouldUpdateEmergencyCallButton" }.ignored().hook {
                    before {
                        (c.findField("mEmergencyButton")?.get(instance) as? View)?.isVisible = false
                        result = true
                    }
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
