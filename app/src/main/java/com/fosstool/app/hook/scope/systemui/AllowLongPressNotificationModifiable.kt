package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import java.lang.reflect.Field

object AllowLongPressNotificationModifiable : YukiBaseHooker() {
    override fun onHook() {
        VariousClass(
            "com.oplusos.systemui.notification.settingspanel.NotificationSettingsModel",
            "com.oplusos.systemui.notification.settingspanel.controller.NotificationController",
            "com.oplus.systemui.statusbar.notification.settingspanel.controller.NotificationController"
        ).toClassOrNull(appClassLoader)?.let { c ->

            c.method {
                name { it.startsWith("resolve") && it.contains("Mode") }
                paramCount = 1
            }.ignored().hook {
                before {
                    val field = c.findField("isAppModifiable")
                    if (field != null) {
                        field.set(instance, true)
                    } else {
                        val arg0 = args.getOrNull(0) ?: return@before
                        arg0.javaClass.findField("isAppModifiable")?.set(arg0, true)
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
