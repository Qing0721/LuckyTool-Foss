package com.fosstool.app.hook.scope.systemui

import android.content.Context
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Field
import java.lang.reflect.Modifier

object RemoveUSBConnectDialog : YukiBaseHooker() {
    override fun onHook() {
        VariousClass(
            "com.coloros.systemui.notification.usb.UsbService",
            "com.oplusos.systemui.notification.usb.UsbService",
            "com.oplus.systemui.usb.UsbService"
        ).toClassOrNull(appClassLoader)?.let { c ->
            c.method { name = "onUsbConnected" }.ignored().hook {
                before {
                    val context = args.getOrNull(0) as? Context ?: return@before
                    runCatching {
                        XposedHelpers.callMethod(instance, "onUsbSelect", 1)
                        XposedHelpers.callMethod(instance, "updateAdbNotification", context)
                        XposedHelpers.callMethod(
                            instance,
                            "updateUsbNotification",
                            context,
                            1
                        )
                        XposedHelpers.callMethod(instance, "changeUsbConfig", context, 1)
                    }
                    result = null
                }
            }

            c.method { name = "updateUsbNotification" }.ignored().hook {
                before {
                    c.findFieldEndsWith("NeedShowUsbDialog")?.let { f ->
                        runCatching { f.set(if (Modifier.isStatic(f.modifiers)) null else instance, false) }
                    }
                }
            }
        }
    }

    private fun Class<*>.findFieldEndsWith(suffix: String): Field? {
        var cls: Class<*>? = this
        while (cls != null && cls != Any::class.java) {
            cls.declaredFields.firstOrNull { it.name.endsWith(suffix, ignoreCase = true) }
                ?.let { return it.apply { isAccessible = true } }
            cls = cls.superclass
        }
        return null
    }
}
