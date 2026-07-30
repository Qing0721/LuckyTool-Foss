package com.fosstool.app.hook.scope.systemui

import android.content.Context
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog
import com.fosstool.app.hook.utils.SettingsUtils
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Field

object ForceEnableScreenOffMusicSupport : YukiBaseHooker() {
    override fun onHook() {
        val c = VariousClass(
            "com.oplus.systemui.keyguard.OplusBlackScreenGestureControllExImpl",
            "com.oplus.systemui.keyguard.gesture.OplusBlackScreenGestureControllExImpl"
        ).toClassOrNull(appClassLoader)
        if (c == null) {
            YLog.error(
                "ForceEnableScreenOffMusicSupport: OplusBlackScreenGestureControllExImpl not found",
                tag = "LuckyTool"
            )
            return
        }

        val hasReset = c.hasMethod("resetAodMediaSupportConfig")
        val targetName = if (hasReset) "resetAodMediaSupportConfig" else "init"

        c.method { name = targetName; superClass() }.ignored().hook {
            after {
                val context = c.findField("mContext")?.get(instance) as? Context ?: return@after
                runCatching {
                    XposedHelpers.callStaticMethod(
                        SettingsUtils(appClassLoader).Secure,
                        "putIntForUser",
                        context.contentResolver,
                        "aod_media_support",
                        1,
                        0
                    )
                }
                val util = VariousClass(
                    "com.oplusos.systemui.notification.util.NotificationStatisticUtil",
                    "com.oplus.systemui.aod.mediapanel.util.AodMediaStatisticUtil"
                ).toClassOrNull(appClassLoader) ?: return@after
                runCatching {
                    util.getDeclaredMethod("setAodMediaSupport", Boolean::class.java)
                        .also { it.isAccessible = true }.invoke(null, true)
                }
            }
        }
    }

    private fun Class<*>.hasMethod(name: String): Boolean {
        var cls: Class<*>? = this
        while (cls != null) {
            if (cls.declaredMethods.any { it.name == name }) return true
            cls = cls.superclass
        }
        return false
    }

    private fun Class<*>.findField(name: String): Field? {
        var cls: Class<*>? = this
        while (cls != null) {
            runCatching { return cls.getDeclaredField(name).also { it.isAccessible = true } }
            cls = cls.superclass
        }
        return null
    }
}
