package com.fosstool.app.hook.scope.android

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.utils.ModulePrefs

object RemoveVPNActiveNotification : YukiBaseHooker() {
    override fun onHook() {
        val isEnable = prefs(ModulePrefs).getBoolean("remove_vpn_active_notification", false)
        if (!isEnable) return

        VariousClass(
            "com.android.server.connectivity.VpnExtImpl",
            "com.android.server.connectivity.OplusVpnHelper",
        ).toClassOrNull(appClassLoader)?.apply {
            method { name = "showNotification" }.ignored().hook { intercept() }
        }
    }
}
