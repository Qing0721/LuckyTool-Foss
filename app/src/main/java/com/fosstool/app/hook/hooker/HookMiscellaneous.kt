package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.externalstorage.RemoveStorageLimit
import com.fosstool.app.hook.scope.systemui.DisableOTGAutoOff
import com.fosstool.app.hook.scope.systemui.RemovePowerMenuSosButton
import com.fosstool.app.hook.scope.systemui.ShowChargingRipple
import com.fosstool.app.hook.scope.systemui.ShowManualLockButtonPowerMenu
import com.fosstool.app.utils.A12
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK

object HookMiscellaneous : YukiBaseHooker() {
    override fun onHook() {
        if (packageName == "com.android.systemui") {
            if (prefs(ModulePrefs).getBoolean("show_charging_ripple", false)) {
                if (SDK >= A12) loadHooker(ShowChargingRipple)
            }
            if (prefs(ModulePrefs).getBoolean("disable_otg_auto_off", false)) {
                loadHooker(DisableOTGAutoOff)
            }
            if (prefs(ModulePrefs).getBoolean("remove_power_menu_sos_button", false)) {
                loadHooker(RemovePowerMenuSosButton)
            }
            if (prefs(ModulePrefs).getBoolean("show_manual_lock_button_power_menu", false)) {
                loadHooker(ShowManualLockButtonPowerMenu)
            }
        }

        if (packageName == "com.android.externalstorage") {
            if (prefs(ModulePrefs).getBoolean("remove_storage_limit", false)) {
                loadHooker(RemoveStorageLimit)
            }
        }

        if (packageName == "com.oplus.exsystemservice") {
            if (prefs(ModulePrefs).getBoolean("disable_otg_auto_off", false)) {
                loadHooker(DisableOTGAutoOff)
            }
        }
    }
}
