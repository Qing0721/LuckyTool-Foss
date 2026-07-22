package com.fosstool.app.hook.scope.systemui

import android.view.View
import androidx.core.view.isVisible
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method

object RemoveWiFiDataInout : YukiBaseHooker() {
    override fun onHook() {
        VariousClass(
            "com.oplusos.systemui.statusbar.OplusStatusBarWifiView",
            "com.oplus.systemui.statusbar.phone.signal.OplusStatusBarWifiViewExImpl"
        ).toClass().apply {
            method { name = "initViewState" }.hook {
                after {
                    field { name = "mWifiActivity" }.get(instance).cast<View>()?.isVisible = false
                }
            }
            method { name = "updateState" }.hook {
                after {
                    field { name = "mWifiActivity" }.get(instance).cast<View>()?.isVisible = false
                }
            }
        }
    }
}
