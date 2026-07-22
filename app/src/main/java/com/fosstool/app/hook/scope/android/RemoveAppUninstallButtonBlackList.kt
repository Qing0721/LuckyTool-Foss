package com.fosstool.app.hook.scope.android

import android.util.ArraySet
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK

object RemoveAppUninstallButtonBlackList : YukiBaseHooker() {
    override fun onHook() {
        val isEnable = prefs(ModulePrefs).getBoolean("remove_app_uninstall_button_blacklist", false)
        if (SDK < A13) return

        "com.android.server.pm.OplusUninstallableConfigManager".toClass().apply {
            method { name = "loadUninstallableConfig" }.hook {
                after {
                    if (!isEnable) return@after
                    val mHideUninstallIcon =
                        field { name = "mHideUninstallIcon" }.get(instance).any()
                    mHideUninstallIcon?.current()?.field { name = "mList" }?.set(ArraySet<String>())
                    val mHideUninstallIconSoft =
                        field { name = "mHideUninstallIconSoft" }.get(instance).any()
                    mHideUninstallIconSoft?.current()?.field { name = "mList" }
                        ?.set(ArraySet<String>())
                }
            }
        }
    }
}
