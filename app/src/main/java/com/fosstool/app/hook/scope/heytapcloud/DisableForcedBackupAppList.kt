package com.fosstool.app.hook.scope.heytapcloud

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.fosstool.app.utils.ModulePrefs

object DisableForcedBackupAppList : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("disable_forced_backup_app_list", false)) return
        runCatching {
            val styleEnum = "com.heytap.cloud.backuprestore.bswitch.BackupRestoreOptUiStyle".toClass()
            val styleSwitch = styleEnum.enumConstants?.firstOrNull { it.toString() == "STYLE_SWITCH" }
            "com.heytap.cloud.backuprestore.bswitch.BackupRestoreOpt".toClass().apply {
                method {
                    name = "getForceSelect"
                    returnType = BooleanType
                }.hook { replaceToFalse() }
            }
            if (styleSwitch != null) {
                "com.heytap.cloud.backuprestore.bswitch.bean.BackupRestoreOptUiData".toClass().apply {
                    method {
                        name = "getOptStyle"
                        returnType = styleEnum
                    }.hook { replaceTo(styleSwitch) }
                }
            }
        }
    }
}
