package com.fosstool.app.hook.scope.notificationmanager

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.fosstool.app.utils.ModulePrefs

object RemoveNotificationPinNumberLimit : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("remove_notification_pin_number_limit", false)) return
        runCatching {
            "com.oplus.notificationmanager.property.uicontroller.AppNotificationTopController".toClass().apply {
                method { returnType = BooleanType }.hookAll { before { resultTrue() } }
            }
        }
    }
}
