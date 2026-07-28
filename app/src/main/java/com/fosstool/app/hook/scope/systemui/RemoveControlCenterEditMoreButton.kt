package com.fosstool.app.hook.scope.systemui

import android.view.View
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.utils.ModulePrefs
import java.lang.reflect.Field

object RemoveControlCenterEditMoreButton : YukiBaseHooker() {
    override fun onHook() {
        val removeEdit = prefs(ModulePrefs).getBoolean("remove_control_center_edit_button", false)
        val removeMore = prefs(ModulePrefs).getBoolean("remove_control_center_more_button", false)
        if (!removeEdit && !removeMore) return

        "com.oplus.systemui.plugins.qs.bottom.OplusQSBottomViewController"
            .toClassOrNull(appClassLoader)
            ?.method { name = "init" }?.ignored()?.hook {
                after {
                    val c = instance.javaClass
                    if (removeEdit) (c.findField("editBtn")?.get(instance) as? View)?.visibility =
                        View.GONE
                    if (removeMore) (c.findField("moreBtn")?.get(instance) as? View)?.visibility =
                        View.GONE
                }
            }
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
