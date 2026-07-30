package com.fosstool.app.hook.scope.android

import android.util.ArraySet
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.utils.ModulePrefs
import de.robv.android.xposed.XposedHelpers

object RemoveAppUninstallButtonBlackList : YukiBaseHooker() {
    override fun onHook() {

        val isEnable = prefs(ModulePrefs).getBoolean("remove_app_uninstall_button_blacklist", false)
        if (!isEnable) return

        val cls = "com.android.server.pm.OplusUninstallableConfigManager".toClassOrNull(appClassLoader) ?: return
        cls.method { name = "loadUninstallableConfig" }.ignored().hook {
            after {
                listOf("mHideUninstallIcon", "mHideUninstallIconSoft").forEach { fieldName ->
                    val holder = runCatching {
                        cls.field { name = fieldName }.ignored().get(instance).any()
                    }.getOrNull()
                        ?: runCatching {
                            XposedHelpers.getObjectField(instance, fieldName)
                        }.getOrNull()
                    if (holder != null) {
                        runCatching {
                            XposedHelpers.setObjectField(holder, "mList", ArraySet<String>())
                        }
                    }
                }
            }
        }
    }
}
