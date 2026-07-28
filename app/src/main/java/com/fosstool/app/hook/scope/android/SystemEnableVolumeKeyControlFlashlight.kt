package com.fosstool.app.hook.scope.android

import android.content.Context
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getOSVersionCode
import de.robv.android.xposed.XposedHelpers

object SystemEnableVolumeKeyControlFlashlight : YukiBaseHooker() {
    override fun onHook() {
        val isEnable = prefs(ModulePrefs).getBoolean("enable_volume_key_control_flashlight", false)
        if (getOSVersionCode < 27) return

        val clazz = "com.android.server.power.OplusScreenOffTorchHelper".toClassOrNull(appClassLoader) ?: return
        clazz.method { name = "getInstance"; paramCount = 1 }.ignored().hook {
            after {
                if (!isEnable) return@after
                val context = args(0).any() as? Context ?: return@after
                if (result == null) {
                    result = XposedHelpers.newInstance(clazz, context)
                }
            }
        }
    }
}
