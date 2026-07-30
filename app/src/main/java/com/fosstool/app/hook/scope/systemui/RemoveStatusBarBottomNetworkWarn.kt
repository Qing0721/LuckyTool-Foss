package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.utils.ModulePrefs

object RemoveStatusBarBottomNetworkWarn : YukiBaseHooker() {
    override fun onHook() {
        var removeMode = prefs(ModulePrefs).getString("remove_control_center_networkwarn", "0")
        dataChannel.wait<String>("remove_control_center_networkwarn") { removeMode = it }

        VariousClass(
            "com.oplusos.systemui.qs.widget.OplusQSSecurityText",
            "com.oplus.systemui.qs.widget.OplusQSSecurityText"
        ).toClassOrNull(appClassLoader)?.let { textCls ->
            textCls.method { name = "handleClick" }.ignored().hook {
                before {
                    if (removeMode == "1" || removeMode == "2") result = null
                }
            }
            textCls.method { name = "handleRefreshState" }.ignored().hook {
                before {
                    if (removeMode == "2") result = null
                }
            }
        }

        VariousClass(
            "com.oplus.systemui.qs.policy.OplusQSSecurityController",
            "com.oplusos.systemui.qs.policy.OplusQSSecurityController"
        ).toClassOrNull(appClassLoader)?.let { ctrl ->
            ctrl.method { name = "showDeviceMonitoringDialog" }.ignored().hook {
                before {
                    if (removeMode == "1" || removeMode == "2") result = null
                }
            }
            ctrl.method { name = "handleRefreshState" }.ignored().hook {
                before {
                    if (removeMode == "2") result = null
                }
            }
        }
    }
}
