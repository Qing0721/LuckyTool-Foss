package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.A14
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK

object BluetoothIconRelated : YukiBaseHooker() {
    override fun onHook() {
        var isHide = prefs(ModulePrefs).getBoolean("hide_icon_when_bluetooth_not_connected", false)
        dataChannel.wait<Boolean>("hide_icon_when_bluetooth_not_connected") { isHide = it }
        VariousClass(
            "com.oplusos.systemui.statusbar.phone.PhoneStatusBarPolicyEx",
            "com.oplus.systemui.statusbar.phone.OplusPhoneStatusBarPolicyExImpl"
        ).toClass().apply {
            method { name = "updateBluetoothIcon";paramCount = 4 }.hook {
                before {
                    if (!isHide) return@before
                    val isBluetoothEnabled = args().last().boolean()
                    val controller = field {
                        if (SDK >= A14) name = "bluetoothController"
                        else {
                            name = "mBluetooth"
                            superClass(true)
                        }
                    }.get(instance).any() ?: return@before
                    val isBluetoothConnected = controller.current().method {
                        name = "isBluetoothConnected"
                    }.invoke<Boolean>() ?: return@before
                    args().last().set(isBluetoothEnabled && isBluetoothConnected)
                }
            }
        }
    }
}
