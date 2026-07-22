package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.heytapcloud.DisableForcedBackupAppList
import com.fosstool.app.hook.scope.heytapcloud.RemoveNetworkRestriction
import com.fosstool.app.utils.ModulePrefs

object HookCloudService : YukiBaseHooker() {
    override fun onHook() {
        if (prefs(ModulePrefs).getBoolean("remove_network_limit",false)) {
            loadHooker(RemoveNetworkRestriction)
        }
        if (prefs(ModulePrefs).getBoolean("disable_forced_backup_app_list", false)) {
            loadHooker(DisableForcedBackupAppList)
        }
    }
}
