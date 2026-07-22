package com.fosstool.app.hook.scope.android

import android.content.Context
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.buildOf
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getOSVersionCode

object SystemEnableVolumeKeyControlFlashlight : YukiBaseHooker() {
    override fun onHook() {
        val isEnable = prefs(ModulePrefs).getBoolean("enable_volume_key_control_flashlight", false)
        if (getOSVersionCode < 27) return

        val clazz = "com.android.server.power.OplusScreenOffTorchHelper".toClassOrNull()
        clazz?.method { name = "getInstance";param(ContextClass) }?.hook {
            after {
                if (!isEnable) return@after
                val context = args().first().cast<Context>() ?: return@after
                if (result == null) result = clazz.buildOf(context) { param(ContextClass) }
            }
        }
    }
}
