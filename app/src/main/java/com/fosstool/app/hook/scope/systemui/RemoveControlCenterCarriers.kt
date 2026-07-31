package com.fosstool.app.hook.scope.systemui

import com.fosstool.app.utils.ModulePrefs
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.constructor
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object RemoveControlCenterCarriers : YukiBaseHooker() {
    private const val TARGET = "com.oplus.systemui.qs.widget.OplusSecondCarrierText"
    private const val FIELD = "mCarrierTextCallback"

    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("remove_control_center_carriers", false)) return

        TARGET.toClassOrNull(appClassLoader)
            ?.constructor()
            ?.ignored()
            ?.hook {
                after {
                    runCatching {
                        instance.javaClass.getDeclaredField(FIELD).apply { isAccessible = true }
                            .set(instance, null)
                    }
                }
            }
    }
}
