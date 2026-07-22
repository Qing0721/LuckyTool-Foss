package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.permissioncontroller.RemoveStoragePermissionExceptionDialog
import com.fosstool.app.hook.scope.permissioncontroller.UnlockDefaultDesktopLimit
import com.fosstool.app.utils.ModulePrefs

object HookPermissionController : YukiBaseHooker() {
    override fun onHook() {
        if (prefs(ModulePrefs).getBoolean("unlock_default_desktop_limit", false)) {
            loadHooker(UnlockDefaultDesktopLimit)
        }
        if (prefs(ModulePrefs).getBoolean("remove_storage_permission_exception_dialog", false)) {
            loadHooker(RemoveStoragePermissionExceptionDialog)
        }
    }
}
