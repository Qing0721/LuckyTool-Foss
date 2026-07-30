package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.constructor
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object RemoveControlCenterCarriers : YukiBaseHooker() {
    private const val TARGET = "com.oplus.systemui.qs.widget.OplusSecondCarrierText"
    private const val FIELD = "mCarrierTextCallback"

    override fun onHook() {

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
