package com.fosstool.app.hook.scope.heytapcloud

import com.fosstool.app.utils.ModulePrefs
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import java.lang.reflect.Method

object DisableForcedBackupAppList : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("disable_forced_backup_app_list", false)) return
        "com.heytap.cloud.backuprestore.bswitch.BackupRestoreOpt".toClassOrNull(appClassLoader)
            ?.findMethod("getForceSelect")
            ?.let { runCatching { XposedBridge.hookMethod(it, XC_MethodReplacement.returnConstant(false)) } }
        val styleEnum = "com.heytap.cloud.backuprestore.bswitch.BackupRestoreOptUiStyle"
            .toClassOrNull(appClassLoader) ?: return
        val styleSwitch = styleEnum.enumConstants?.firstOrNull { it.toString() == "STYLE_SWITCH" }
            ?: return
        "com.heytap.cloud.backuprestore.bswitch.bean.BackupRestoreOptUiData".toClassOrNull(appClassLoader)
            ?.findMethod("getOptStyle")
            ?.let { runCatching { XposedBridge.hookMethod(it, XC_MethodReplacement.returnConstant(styleSwitch)) } }
    }

    private fun Class<*>.findMethod(name: String): Method? {
        var c: Class<*>? = this
        while (c != null && c != Any::class.java) {
            c.declaredMethods.firstOrNull { it.name == name }?.let { return it.apply { isAccessible = true } }
            c = c.superclass
        }
        return null
    }
}
