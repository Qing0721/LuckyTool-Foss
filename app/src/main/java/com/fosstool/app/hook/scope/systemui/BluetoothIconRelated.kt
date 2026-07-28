package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.utils.ModulePrefs
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Field

object BluetoothIconRelated : YukiBaseHooker() {

    private const val BLUETOOTH_CONTROLLER =
        "com.android.systemui.statusbar.policy.BluetoothController"
    private const val STATUS_BAR_ICON_CONTROLLER =
        "com.android.systemui.statusbar.phone.ui.StatusBarIconController"

    override fun onHook() {
        var isHide = prefs(ModulePrefs).getBoolean("hide_icon_when_bluetooth_not_connected", false)
        dataChannel.wait<Boolean>("hide_icon_when_bluetooth_not_connected") { isHide = it }

        val clazz = VariousClass(
            "com.oplusos.systemui.statusbar.phone.PhoneStatusBarPolicyEx",
            "com.oplus.systemui.statusbar.phone.OplusPhoneStatusBarPolicyExImpl"
        ).toClassOrNull(appClassLoader) ?: return

        val hasIconMethod = clazz.findMethod("updateBluetoothIcon", 4) != null
        if (hasIconMethod) {
            clazz.method { name = "updateBluetoothIcon"; paramCount = 4 }.ignored().hook {
                before {
                    if (!isHide) return@before
                    if (args.isEmpty()) return@before
                    val last = args.size - 1
                    val isBluetoothEnabled = args[last] as? Boolean ?: return@before
                    val controller = clazz.findFieldOfType(BLUETOOTH_CONTROLLER)
                        ?.get(instance) ?: return@before
                    val connected = runCatching {
                        XposedHelpers.callMethod(controller, "isBluetoothConnected") as? Boolean
                    }.getOrNull() ?: return@before
                    args[last] = isBluetoothEnabled && connected
                }
            }
            return
        }

        clazz.method { name = "updateBluetooth"; emptyParam() }.ignored().hook {
            before {
                if (!isHide) return@before
                val controller = clazz.findFieldOfType(BLUETOOTH_CONTROLLER)
                    ?.get(instance) ?: return@before
                val iconController = clazz.findFieldOfType(STATUS_BAR_ICON_CONTROLLER)
                    ?.get(instance) ?: return@before
                val slotBluetooth = clazz.findField("slotBluetooth")?.get(instance) as? String
                    ?: return@before
                val enabled = controller.javaClass.findField("mEnabled")
                    ?.get(controller) as? Boolean ?: false
                val state = controller.javaClass.findField("mConnectionState")
                    ?.get(controller) as? Int
                if (enabled && state != 2) {
                    runCatching {
                        XposedHelpers.callMethod(
                            iconController, "setIconVisibility", slotBluetooth, false
                        )
                    }
                    resultNull()
                }
            }
        }
    }

    private fun Class<*>.findMethod(name: String, paramCount: Int): java.lang.reflect.Method? {
        var cls: Class<*>? = this
        while (cls != null) {
            cls.declaredMethods.firstOrNull { it.name == name && it.parameterCount == paramCount }
                ?.let { return it }
            cls = cls.superclass
        }
        return null
    }

    private fun Class<*>.findFieldOfType(typeName: String): Field? {
        var cls: Class<*>? = this
        while (cls != null) {
            cls.declaredFields.firstOrNull { it.type.isTypeOf(typeName) }
                ?.let { f -> return f.also { it.isAccessible = true } }
            cls = cls.superclass
        }
        return null
    }

    private fun Class<*>.isTypeOf(typeName: String): Boolean {
        if (name == typeName) return true
        if (interfaces.any { it.isTypeOf(typeName) }) return true
        return superclass?.isTypeOf(typeName) == true
    }

    private fun Class<*>.findField(name: String): Field? {
        var cls: Class<*>? = this
        while (cls != null) {
            runCatching { return cls.getDeclaredField(name).also { it.isAccessible = true } }
            cls = cls.superclass
        }
        return null
    }
}
