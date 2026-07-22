package com.fosstool.app.hook.scope.systemui

import android.content.Context
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.A14
import com.fosstool.app.utils.SDK

object RemoveUSBConnectDialog : YukiBaseHooker() {
    override fun onHook() {
        VariousClass(
            "com.coloros.systemui.notification.usb.UsbService",
            "com.oplusos.systemui.notification.usb.UsbService",
            "com.oplus.systemui.usb.UsbService"
        ).toClass().apply {
            method { name = "onUsbConnected" }.hook {
                replaceUnit {
                    val context = args().first().cast<Context>() ?: return@replaceUnit
                    method { name = "onUsbSelect" }.get(instance).call(1)
                    method { name = "updateAdbNotification" }.get(instance).call(context)
                    method { name = "updateUsbNotification" }.get(instance).call(context, 1)
                    method { name = "changeUsbConfig" }.get(instance).call(context, 1)
                }
            }
            if (SDK >= A14) method { name = "helpUpdateUsbNotification" }.hook {
                before { field { name = "mNeedShowUsbDialog" }.get(instance).setFalse() }
            } else method { name = "updateUsbNotification" }.hook {
                before { field { name = "sNeedShowUsbDialog" }.get().setFalse() }
            }
        }
    }
}
