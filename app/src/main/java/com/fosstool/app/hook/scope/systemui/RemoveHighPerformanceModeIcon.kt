package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import java.lang.reflect.Field

object RemoveHighPerformanceModeIcon : YukiBaseHooker() {
    override fun onHook() {
        VariousClass(
            "com.oplusos.systemui.statusbar.phone.PhoneStatusBarPolicyEx",
            "com.oplus.systemui.statusbar.phone.OplusPhoneStatusBarPolicyExImpl"
        ).toClassOrNull(appClassLoader)?.let { c ->
            c.method { name = "updateHighPerformanceIcon"; paramCount = 0 }.ignored().hook {
                before {
                    c.findField("highPerformanceMode")?.set(instance, false)
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
