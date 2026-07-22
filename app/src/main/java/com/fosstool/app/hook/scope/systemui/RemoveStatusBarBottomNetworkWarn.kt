package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.hasMethod
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.ModulePrefs

object RemoveStatusBarBottomNetworkWarn : YukiBaseHooker() {
    override fun onHook() {
        var removeMode = prefs(ModulePrefs).getString("remove_control_center_networkwarn", "0")
        dataChannel.wait<String>("remove_control_center_networkwarn") { removeMode = it }

        VariousClass(
            "com.oplusos.systemui.qs.widget.OplusQSSecurityText",
            "com.oplus.systemui.qs.widget.OplusQSSecurityText"
        ).toClass().apply {
            method { name = "handleClick" }.hook {
                if (removeMode == "1" || removeMode == "2") intercept()
            }
            method { name = "handleRefreshState" }.hook {
                if (removeMode == "2") intercept()
            }
        }

        "com.oplus.systemui.qs.policy.OplusQSSecurityController".toClass().apply {
            if (hasMethod { name = "showDeviceMonitoringDialog" }) {
                method { name = "showDeviceMonitoringDialog" }.hook {
                    if (removeMode == "1" || removeMode == "2") intercept()
                }
            }
            if (hasMethod { name = "handleRefreshState" }) {
                method { name = "handleRefreshState" }.hook {
                    if (removeMode == "2") intercept()
                }
            }
        }
    }
}
