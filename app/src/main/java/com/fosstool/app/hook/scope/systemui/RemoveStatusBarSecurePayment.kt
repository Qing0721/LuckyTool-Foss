package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method

object RemoveStatusBarSecurePayment : YukiBaseHooker() {
    override fun onHook() {
        VariousClass(
            "com.oplusos.systemui.ext.SecurePaymentControllerExt",
            "com.oplus.systemui.statusbar.phone.securepay.SecurePaymentControllerExImpl",
            "com.oplus.systemui.statusbar.phone.dynamic.SecurePaymentController"
        ).toClass().apply {
            method { name = "handlePaymentDetectionMessage" }.hook {
                intercept()
            }
        }
    }
}
