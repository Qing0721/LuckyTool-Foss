package com.fosstool.app.hook.scope.android

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method

object DisableAccessibilityWarningDialog : YukiBaseHooker() {
    override fun onHook() {
        runCatching {
            "com.android.server.am.FraudBehaviorDetectManager".toClass().apply {
                method { name = "updateGlobalCloseConfigToXmlFile" }.hookAll {
                    intercept()
                }
                method { name = "showWarningDialog" }.hookAll {
                    intercept()
                }
                method { name = "showAccessibilityWarning" }.hookAll {
                    intercept()
                }
            }
        }
    }
}
