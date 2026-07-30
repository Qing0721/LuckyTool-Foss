package com.fosstool.app.hook.scope.notificationmanager

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.java.AnyClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType

object RemoveNotificationPinNumberLimit : YukiBaseHooker() {
    override fun onHook() {
        val clazz = VariousClass(
            "com.oplus.notificationmanager.property.uicontroller.AppNotificationTopController",
            "com.coloros.notificationmanager.property.uicontroller.AppNotificationTopController",
        ).toClassOrNull(appClassLoader) ?: return
        val preference = "androidx.preference.Preference".toClassOrNull(appClassLoader) ?: return

        clazz.method {
            param(clazz, preference, AnyClass)
            returnType = BooleanType
        }.ignored().hookAll {
            before {
                val controller = args.getOrNull(0) ?: return@before
                val newValue = args.lastOrNull() as? Boolean ?: false
                runCatching {
                    controller.current().method { name = "onChange" }.call(newValue)
                }
                resultTrue()
            }
        }
    }
}
