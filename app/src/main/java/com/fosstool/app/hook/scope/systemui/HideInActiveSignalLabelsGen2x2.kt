package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog
import java.lang.reflect.Field

object HideInActiveSignalLabelsGen2x2 : YukiBaseHooker() {
    override fun onHook() {
        val cls = VariousClass(
            "com.oplus.systemui.statusbar.policy.MobileIconSets",
            "com.oplusos.systemui.statusbar.policy.MobileIconSets"
        ).toClassOrNull(appClassLoader)
        if (cls == null) {
            YLog.error("HideInActiveSignalLabelsGen2x2: MobileIconSets not found", tag = "LuckyTool")
            return
        }
        val volteIcon = cls.findField("VOLTE_ICON") ?: return
        val volteIconEx = cls.findField("VOLTE_ICON_EX") ?: return
        val value = runCatching { volteIconEx.get(null) as? IntArray }.getOrNull() ?: return
        runCatching { volteIcon.set(null, value) }
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
