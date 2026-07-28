package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.permissioncontroller.RemoveStoragePermissionExceptionDialog
import com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker
import com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker.Mode
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getOSVersionCode

object HookPermissionController : YukiBaseHooker() {
    override fun onHook() {
        loadHooker(SystemPropertiesOverrideEngineHooker(mode = Mode.RM0_Q))

        if (getOSVersionCode < 37 &&
            prefs(ModulePrefs).getBoolean("remove_storage_permission_exception_dialog", false)
        ) {
            loadHooker(RemoveStoragePermissionExceptionDialog)
        }
    }
}
