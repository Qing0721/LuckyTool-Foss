package com.fosstool.app.hook.scope.systemui

import android.content.Context
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog
import com.fosstool.app.utils.ModulePrefs
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Field

object DisableHighVolumeWarningNotification : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("disable_high_volume_warning_notifications", false)) return

        val receiverCls = VariousClass(
            "com.oplusos.systemui.notification.receiver.VolumeReceiver",
            "com.oplus.systemui.statusbar.receiver.VolumeReceiver"
        ).toClassOrNull(appClassLoader)

        val powerUiCls = VariousClass(
            "com.oplusos.systemui.notification.power.OplusPowerUI",
            "com.oplus.systemui.statusbar.notification.power.OplusPowerUI"
        ).toClassOrNull(appClassLoader)
        if (powerUiCls == null) {
            YLog.error(
                "DisableHighVolumeWarningNotification: OplusPowerUI not found",
                tag = "LuckyTool"
            )
            return
        }

        powerUiCls.method { name = "start" }.ignored().hook {
            after {
                val context = powerUiCls.findFieldOfType(Context::class.java)
                    ?.get(instance) as? Context ?: return@after
                val receiverField = receiverCls?.let { powerUiCls.findFieldOfType(it) }
                    ?: powerUiCls.findFieldByNameContains("VolumeReceiver")
                    ?: return@after
                val receiver = receiverField.get(instance) ?: return@after
                runCatching { XposedHelpers.callMethod(receiver, "unregister", context) }
            }
        }
    }

    private fun Class<*>.findFieldOfType(type: Class<*>): Field? {
        var cls: Class<*>? = this
        while (cls != null) {
            cls.declaredFields.firstOrNull { type.isAssignableFrom(it.type) }
                ?.let { f -> return f.also { it.isAccessible = true } }
            cls = cls.superclass
        }
        return null
    }

    private fun Class<*>.findFieldByNameContains(keyword: String): Field? {
        var cls: Class<*>? = this
        while (cls != null) {
            cls.declaredFields.firstOrNull { it.name.contains(keyword, ignoreCase = true) }
                ?.let { f -> return f.also { it.isAccessible = true } }
            cls = cls.superclass
        }
        return null
    }
}
