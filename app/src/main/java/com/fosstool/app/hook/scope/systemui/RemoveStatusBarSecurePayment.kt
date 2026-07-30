package com.fosstool.app.hook.scope.systemui

import android.os.Message
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object RemoveStatusBarSecurePayment : YukiBaseHooker() {
    override fun onHook() {

        VariousClass(
            "com.oplus.systemui.statusbar.phone.securepay.SecurePaymentControllerExImpl",
            "com.oplus.systemui.statusbar.phone.dynamic.SecurePaymentController",
            "com.oplusos.systemui.ext.SecurePaymentControllerExt"
        ).toClassOrNull(appClassLoader)
            ?.method { name = "handlePaymentDetectionMessage"; param(Message::class.java) }
            ?.ignored()?.hook { intercept() }
    }
}
